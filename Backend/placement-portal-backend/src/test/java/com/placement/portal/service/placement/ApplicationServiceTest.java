package com.placement.portal.service.placement;

import com.placement.portal.domain.Application;
import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.Job;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.enums.ApplicationStatus;
import com.placement.portal.dto.request.ApplicationRequest;
import com.placement.portal.dto.request.ApplicationStatusUpdateRequest;
import com.placement.portal.dto.response.ApplicationDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.ApplicationRepository;
import com.placement.portal.repository.InternshipRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.ResumeRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ApplicationService}.
 *
 * <p>Locks <b>B4</b> — when an application transitions to ACCEPTED, the student is
 * marked placed AND the placement package + company are written to their profile.
 * Without this, the placement-officer dashboard reports zero average package.</p>
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private JobRepository jobRepository;
    @Mock private InternshipRepository internshipRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private EntityMapper entityMapper;
    @Mock private SecurityUtils securityUtils;

    private ApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(
                applicationRepository,
                studentProfileRepository,
                jobRepository,
                internshipRepository,
                resumeRepository,
                entityMapper,
                securityUtils
        );
    }

    @Test
    void updateStatus_toAccepted_marksStudentPlacedWithPackageAndCompany() {
        StudentProfile student = StudentProfile.builder().id("sp-1").build();
        EmployerProfile employer = EmployerProfile.builder()
                .id("emp-1").companyName("Acme Corp").build();
        Job job = Job.builder()
                .id("job-1")
                .employer(employer)
                .salaryMin(new BigDecimal("800000"))
                .salaryMax(new BigDecimal("1200000"))
                .build();
        Application application = Application.builder()
                .id("app-1")
                .student(student)
                .job(job)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entityMapper.toApplicationDto(any())).thenReturn(new ApplicationDto());

        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setStatus(ApplicationStatus.ACCEPTED);

        service.updateApplicationStatus("app-1", req);

        ArgumentCaptor<StudentProfile> captor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository).save(captor.capture());

        StudentProfile saved = captor.getValue();
        assertThat(saved.isPlaced()).as("B4: student must be flagged as placed").isTrue();
        assertThat(saved.getPlacedCompany()).as("B4: placedCompany must be set").isEqualTo("Acme Corp");
        assertThat(saved.getPlacementPackage())
                .as("B4: placementPackage must be set from job salary (was always null before fix)")
                .isEqualByComparingTo("1200000");
    }

    @Test
    void updateStatus_toRejected_doesNotChangePlacementFlags() {
        StudentProfile student = StudentProfile.builder().id("sp-1").build();
        Application application = Application.builder()
                .id("app-1").student(student).status(ApplicationStatus.SUBMITTED).build();

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entityMapper.toApplicationDto(any())).thenReturn(new ApplicationDto());

        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setStatus(ApplicationStatus.REJECTED);

        service.updateApplicationStatus("app-1", req);

        verify(studentProfileRepository, never()).save(any());
        assertThat(student.isPlaced()).isFalse();
    }

    @Test
    void applyToJob_belowMinCgpa_throws() {
        StudentProfile student = StudentProfile.builder()
                .id("sp-1")
                .cgpa(new BigDecimal("6.0"))
                .build();
        Job job = Job.builder().id("job-1").minCgpa(new BigDecimal("8.0")).build();

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(studentProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(student));
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByStudentIdAndJobId("sp-1", "job-1")).thenReturn(false);

        ApplicationRequest req = new ApplicationRequest();
        req.setJobId("job-1");

        assertThatThrownBy(() -> service.applyToJob(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("minimum requirement");
    }
}
