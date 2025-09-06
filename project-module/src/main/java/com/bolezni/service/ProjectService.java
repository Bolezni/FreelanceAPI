package com.bolezni.service;

import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import com.bolezni.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectDto createProject(ProjectCreateDto projectCreateDto);

    ProjectDto updateProject(Long projectId, ProjectUpdateDto projectUpdateDto);

    void deleteProject(Long id);

    ProjectDto getProjectById(Long id);

    Page<ProjectDto> getProjects(Pageable pageable);

    Page<ProjectDto> getProjectsCurrentUser(Pageable pageable);

    void assignProjectToFreelancer(Long projectId, String freelancerId);

    ProjectDto updateStatus(Long projectId, ProjectStatus status);
}
