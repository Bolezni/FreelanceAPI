package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import com.bolezni.model.ProjectStatus;
import com.bolezni.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(@RequestBody @Valid ProjectCreateDto createDto) {
        ProjectDto dto = projectService.createProject(createDto);
        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("Project created successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(@PathVariable(name = "id") Long id,
                                                                 @RequestBody @Valid ProjectUpdateDto updateDto) {
        ProjectDto dto = projectService.updateProject(id, updateDto);
        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("Project updated successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable(name = "id") Long id) {
        ProjectDto dto = projectService.getProjectById(id);
        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("The project was successfully received")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable(name = "id") Long id) {
        projectService.deleteProject(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("The project was successfully received")
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getAllProjects(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectDto> projectDtos = projectService.getProjects(pageable);
        ApiResponse<Page<ProjectDto>> apiResponse = ApiResponse.<Page<ProjectDto>>builder()
                .status(true)
                .data(projectDtos)
                .message("Request successfully completed ")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getAllProjectsCurrentUser(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectDto> projectDtos = projectService.getProjectsCurrentUser(pageable);
        ApiResponse<Page<ProjectDto>> apiResponse = ApiResponse.<Page<ProjectDto>>builder()
                .status(true)
                .data(projectDtos)
                .message("Request successfully completed ")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }

    @PostMapping("/{projectId}/assign/{freelanceId}")
    public ResponseEntity<ApiResponse<Void>> assignProject(@PathVariable(name = "projectId") @Positive Long projectId,
                                                           @PathVariable(name = "freelanceId") @NotBlank String freelanceId) {
        projectService.assignProjectToFreelancer(projectId, freelanceId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("Project assigned successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{projectId}/unassign/{freelancerId}")
    public ResponseEntity<ApiResponse<Void>> unassignProject(@PathVariable(name = "projectId") @Positive Long projectId,
                                                             @PathVariable(name = "freelancerId") @NotBlank String freelancerId){
        projectService.unassignProjectFromFreelancer(projectId, freelancerId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("Project unassigned successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateStatus(@PathVariable(name = "id") Long id,
                                                                @RequestParam @NotNull ProjectStatus status) {
        ProjectDto dto = projectService.updateStatus(id, status);

        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("Project updated successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
