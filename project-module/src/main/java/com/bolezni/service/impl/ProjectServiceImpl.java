package com.bolezni.service.impl;

import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import com.bolezni.mapper.ProjectMapper;
import com.bolezni.model.CategoriesEntity;
import com.bolezni.model.ProjectEntity;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.CategoryRepository;
import com.bolezni.repository.ProjectRepository;
import com.bolezni.service.ProjectService;
import com.bolezni.utils.UpdateFieldUtils;
import com.bolezni.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final CategoryRepository categoryRepository;


    @Override
    @Transactional
    public ProjectDto createProject(ProjectCreateDto projectCreateDto) {
        if (projectCreateDto == null) {
            log.error("projectCreateDto is null");
            throw new RuntimeException("projectCreateDto is null");
        }
        UserEntity author = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found or non authorized"));

        ProjectEntity project = projectMapper.mapProjectCreateToProjectDto(projectCreateDto);
        project.setAuthor(author);

        if (!projectCreateDto.categories().isEmpty()) {
            Set<String> cleanCategories = projectCreateDto.categories().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            Set<CategoriesEntity> categories = findOrCreateCategories(cleanCategories);
            project.setCategories(categories);
        }

        ProjectEntity savedProject = projectRepository.saveAndFlush(project);
        log.info("Project created with id {}", savedProject.getId());

        return projectMapper.mapProjectEntityToDto(savedProject);
    }

    @Override
    @Transactional
    public ProjectDto updateProject(Long projectId, ProjectUpdateDto updateDto) {
        if (updateDto == null) {
            log.error("projectUpdateDto is null");
            throw new RuntimeException("projectUpdateDto is null");
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        UserEntity user = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found or not logged in"));

        if (!user.getId().equals(project.getAuthor().getId())) {
            log.error("User not logged in");
            throw new RuntimeException("User not logged in");
        }

        boolean hasChanges = updateFiledProjectEntity(project, updateDto);

        if (!hasChanges) {
            log.info("Project has changes");
            return projectMapper.mapProjectEntityToDto(project);
        }

        ProjectEntity savedProject = projectRepository.save(project);

        return projectMapper.mapProjectEntityToDto(savedProject);
    }

    private boolean updateFiledProjectEntity(ProjectEntity project, ProjectUpdateDto updateDto) {
        return UpdateFieldUtils.updateMultipleFields(
                () -> UpdateFieldUtils.updateStringField(updateDto.title(), project::getTitle, project::setTitle),
                () -> UpdateFieldUtils.updateStringField(updateDto.description(), project::getDescription, project::setDescription),
                () -> UpdateFieldUtils.updateNumericField(project.getPrice(), updateDto.price(), project::setPrice, false),
                () -> updateCategoriesField(project, updateDto.categories())
        );
    }

    private boolean updateCategoriesField(ProjectEntity project, Set<String> categoryNames) {
        if (categoryNames == null) {
            return false;
        }

        Set<String> cleanCategories = categoryNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> currentCategoryNames = project.getCategories() != null
                ? project.getCategories().stream()
                .map(CategoriesEntity::getName)
                .collect(Collectors.toSet())
                : Set.of();

        if (Objects.equals(currentCategoryNames, cleanCategories)) {
            return false;
        }

        if (cleanCategories.isEmpty()) {
            project.setCategories(Set.of());
        } else {
            Set<CategoriesEntity> categories = findOrCreateCategories(cleanCategories);
            project.setCategories(categories);
        }

        return true;
    }

    private Set<CategoriesEntity> findOrCreateCategories(Set<String> categories) {
        Set<CategoriesEntity> existingCategories = categoryRepository.findByNameIn(categories);

        Set<String> existingCategoriesNames = existingCategories.stream()
                .map(CategoriesEntity::getName)
                .collect(Collectors.toSet());

        Set<String> categoriesToCreate = categories.stream()
                .filter(name -> !existingCategoriesNames.contains(name))
                .collect(Collectors.toSet());

        Set<CategoriesEntity> newCategories = new HashSet<>();
        for (String name : categoriesToCreate) {
            try {
                CategoriesEntity newCategory = categoryRepository.save(
                        CategoriesEntity.builder()
                                .name(name)
                                .build()
                );
                newCategories.add(newCategory);
            } catch (DataIntegrityViolationException e) {

                CategoriesEntity existing = categoryRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Failed to handle duplicate category"));
                newCategories.add(existing);
            }
        }

        Set<CategoriesEntity> result = new HashSet<>(existingCategories);
        result.addAll(newCategories);

        return result;
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        UserEntity user = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        if (projectRepository.existsByAuthorId(user.getId())) {
            projectRepository.deleteById(id);
        } else
            throw new RuntimeException("Project not exists");
    }

    @Override
    public ProjectDto getProjectById(Long id) {
        ProjectEntity projectEntity = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return projectMapper.mapProjectEntityToDto(projectEntity);
    }

    @Override
    public Page<ProjectDto> getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<ProjectEntity> pageEntities = projectRepository.findAll(pageable);

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }

    @Override
    public Page<ProjectDto> getProjectsCurrentUser(int page, int size) {
        UserEntity user = UserUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not logged in"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));

        Page<ProjectEntity> pageEntities = projectRepository.findAllByAuthorId(pageable, user.getId());

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }
}
