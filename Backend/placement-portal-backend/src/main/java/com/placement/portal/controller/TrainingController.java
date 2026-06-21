package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.TrainingModuleDto;
import com.placement.portal.service.placement.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only training catalog for authenticated students. Admin-side
 * create/update/delete endpoints live in {@code AdminController} under
 * {@code /api/admin/training-modules}.
 */
@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/modules")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<TrainingModuleDto>>> listModules() {
        return ResponseEntity.ok(ApiResponse.success(trainingService.listAll()));
    }
}
