package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import com.bolezni.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
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
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(@PathVariable Long id, @RequestBody @Valid ProjectUpdateDto updateDto) {
        ProjectDto dto = projectService.updateProject(id, updateDto);
        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("Project updated successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable Long id) {
        ProjectDto dto = projectService.getProjectById(id);
        ApiResponse<ProjectDto> apiResponse = ApiResponse.<ProjectDto>builder()
                .status(true)
                .data(dto)
                .message("The project was successfully received")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("The project was successfully received")
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getAllProjects(@RequestParam(name="page", defaultValue = "0") int page,
                                                                        @RequestParam(name="size", defaultValue = "10") int size) {
        Page<ProjectDto> projectDtos = projectService.getProjects(page, size);
        ApiResponse<Page<ProjectDto>> apiResponse = ApiResponse.<Page<ProjectDto>>builder()
                .status(true)
                .data(projectDtos)
                .message("Request successfully completed ")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/all-current-user/{userId}")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getAllProjectsCurrentUser(@RequestParam(name="page", defaultValue = "0") int page,
                                                                                   @RequestParam(name="size", defaultValue = "10") int size,
                                                                                   @PathVariable(name = "userId") String userId) {
        Page<ProjectDto> projectDtos = projectService.getProjectsCurrentUser(page, size,userId);
        ApiResponse<Page<ProjectDto>> apiResponse = ApiResponse.<Page<ProjectDto>>builder()
                .status(true)
                .data(projectDtos)
                .message("Request successfully completed ")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }
}
