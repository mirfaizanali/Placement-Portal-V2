package com.placement.portal.service.placement;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.Job;
import com.placement.portal.domain.PlacementDrive;
import com.placement.portal.domain.PlacementOfficerProfile;
import com.placement.portal.domain.enums.DriveStatus;
import com.placement.portal.dto.request.PlacementDriveCreateRequest;
import com.placement.portal.dto.response.PlacementDriveDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.PlacementDriveRepository;
import com.placement.portal.repository.PlacementOfficerProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlacementDriveService {

    private final PlacementDriveRepository driveRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PlacementOfficerProfileRepository officerProfileRepository;
    private final JobRepository jobRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new placement drive.  Caller must hold PLACEMENT_OFFICER role.
     * The first associated job's employer is used as the drive employer.
     */
    public PlacementDriveDto createDrive(PlacementDriveCreateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        log.info("Creating placement drive for officer-user {} with payload {}", userId, req);

        PlacementOfficerProfile officer = officerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "PlacementOfficerProfile for user " + userId + " not found"));

        if (req.getJobIds() == null || req.getJobIds().isEmpty()) {
            log.warn("Drive creation rejected for officer {}: empty jobIds", officer.getId());
            throw new IllegalArgumentException(
                    "At least one jobId is required to determine the employer for this drive");
        }

        Set<Job> jobs = new HashSet<>();
        EmployerProfile employer = null;
        for (String jobId : req.getJobIds()) {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));

            if (job.getEmployer() == null) {
                log.warn("Drive creation rejected for officer {}: job {} has no employer",
                        officer.getId(), jobId);
                throw new IllegalArgumentException(
                        "Job " + jobId + " has no associated employer; cannot create drive");
            }

            if (employer == null) {
                employer = job.getEmployer();
            } else if (!employer.getId().equals(job.getEmployer().getId())) {
                log.warn("Drive creation rejected for officer {}: jobs span multiple employers ({} vs {})",
                        officer.getId(), employer.getId(), job.getEmployer().getId());
                throw new IllegalArgumentException(
                        "Selected jobs belong to multiple employers; a drive must be tied to a single employer");
            }
            jobs.add(job);
        }

        PlacementDrive drive = PlacementDrive.builder()
                .employer(employer)
                .organizedBy(officer)
                .title(req.getTitle())
                .driveDate(req.getDriveDate())
                .venue(req.getVenue())
                .mode(req.getMode())
                .status(DriveStatus.SCHEDULED)
                .description(req.getDescription())
                .jobs(jobs)
                .build();

        PlacementDrive saved = driveRepository.save(drive);
        log.info("Created placement drive {} for employer {} with {} job(s)",
                saved.getId(), employer.getId(), jobs.size());
        return entityMapper.toPlacementDriveDto(saved);
    }

    /**
     * Retrieves a placement drive by its UUID.
     */
    @Transactional(readOnly = true)
    public PlacementDriveDto getDriveById(String id) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PlacementDrive", id));
        return entityMapper.toPlacementDriveDto(drive);
    }

    /**
     * Returns a paginated list of placement drives, optionally filtered by status.
     */
    @Transactional(readOnly = true)
    public Page<PlacementDriveDto> getAllDrives(String status, Pageable pageable) {
        List<PlacementDrive> drives;
        if (status != null) {
            DriveStatus driveStatus = DriveStatus.valueOf(status.toUpperCase());
            drives = driveRepository.findByStatus(driveStatus);
        } else {
            drives = driveRepository.findAll();
        }
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), drives.size());
        List<PlacementDrive> slice = (start > drives.size()) ? List.of() : drives.subList(start, end);
        return new PageImpl<>(
                slice.stream().map(entityMapper::toPlacementDriveDto).toList(),
                pageable, drives.size());
    }

    /**
     * Updates the status of a placement drive.
     */
    public PlacementDriveDto updateDriveStatus(String id, DriveStatus status) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PlacementDrive", id));
        drive.setStatus(status);
        return entityMapper.toPlacementDriveDto(driveRepository.save(drive));
    }
}
