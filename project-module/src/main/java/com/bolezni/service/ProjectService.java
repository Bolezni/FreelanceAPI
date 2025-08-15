package com.bolezni.service;

import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import org.springframework.data.domain.Page;

public interface ProjectService {
    ProjectDto createProject(ProjectCreateDto projectCreateDto);

    ProjectDto updateProject(Long projectId, ProjectUpdateDto projectUpdateDto);

    void deleteProject(Long id);

    ProjectDto getProjectById(Long id);

    Page<ProjectDto> getProjects(int page, int size);

    Page<ProjectDto> getProjectsCurrentUser(int page, int size, String userId);
}
