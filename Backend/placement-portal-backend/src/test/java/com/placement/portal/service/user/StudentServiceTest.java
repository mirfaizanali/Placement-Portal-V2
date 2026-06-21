package com.placement.portal.service.user;

import com.placement.portal.domain.StudentProfile;
import com.placement.portal.dto.request.StudentProfileUpdateRequest;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StudentService}.
 *
 * <p>Locks <b>B1</b> — preferredLocations and preferredJobTypes on the
 * profile-update request are actually written to the persisted entity.</p>
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private FacultyProfileRepository facultyProfileRepository;
    @Mock private EntityMapper entityMapper;
    @Mock private SecurityUtils securityUtils;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(
                studentProfileRepository,
                facultyProfileRepository,
                entityMapper,
                securityUtils
        );
    }

    @Test
    void updateMyProfile_persistsPreferredLocationsAndJobTypes() {
        String userId = "user-1";
        StudentProfile existing = StudentProfile.builder()
                .id("sp-1")
                .userId(userId)
                .build();

        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(entityMapper.toStudentProfileDto(any())).thenReturn(new StudentProfileDto());

        StudentProfileUpdateRequest req = new StudentProfileUpdateRequest();
        req.setPreferredLocations("Bangalore, Pune");
        req.setPreferredJobTypes("FULL_TIME,INTERNSHIP");

        service.updateMyProfile(req);

        ArgumentCaptor<StudentProfile> captor = ArgumentCaptor.forClass(StudentProfile.class);
        org.mockito.Mockito.verify(studentProfileRepository).save(captor.capture());
        StudentProfile saved = captor.getValue();

        assertThat(saved.getPreferredLocations())
                .as("B1: preferredLocations must be persisted")
                .isEqualTo("Bangalore, Pune");
        assertThat(saved.getPreferredJobTypes())
                .as("B1: preferredJobTypes must be persisted")
                .isEqualTo("FULL_TIME,INTERNSHIP");
    }

    @Test
    void updateMyProfile_nullFields_doNotOverwriteExisting() {
        String userId = "user-1";
        StudentProfile existing = StudentProfile.builder()
                .id("sp-1")
                .userId(userId)
                .preferredLocations("Remote")
                .preferredJobTypes("FULL_TIME")
                .build();

        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(entityMapper.toStudentProfileDto(any())).thenReturn(new StudentProfileDto());

        StudentProfileUpdateRequest req = new StudentProfileUpdateRequest(); // all nulls

        service.updateMyProfile(req);

        ArgumentCaptor<StudentProfile> captor = ArgumentCaptor.forClass(StudentProfile.class);
        org.mockito.Mockito.verify(studentProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredLocations()).isEqualTo("Remote");
        assertThat(captor.getValue().getPreferredJobTypes()).isEqualTo("FULL_TIME");
    }
}
