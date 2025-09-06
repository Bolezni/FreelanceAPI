package com.bolezni.service.impl;

import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.dto.ProjectUpdateDto;
import com.bolezni.mapper.ProjectMapper;
import com.bolezni.model.CategoriesEntity;
import com.bolezni.model.ProjectEntity;
import com.bolezni.model.ProjectStatus;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.CategoryRepository;
import com.bolezni.repository.ProjectRepository;
import com.bolezni.repository.UserRepository;
import com.bolezni.service.ProjectService;
import com.bolezni.utils.UpdateFieldUtils;
import com.bolezni.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final long MAX_ACTIVE_PROJECTS = 5L;

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


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

        LocalDateTime now = LocalDateTime.now();

        if (projectCreateDto.deadline().isBefore(now) || projectCreateDto.deadline().isEqual(now)) {
            log.warn("Attempt to set deadline in the past: {}", projectCreateDto.deadline());
            throw new IllegalArgumentException("Deadline must be in the future");
        }

        LocalDateTime maxDeadline = now.plusYears(2);
        if (projectCreateDto.deadline().isAfter(maxDeadline)) {
            log.warn("Deadline too far in the future: {}", projectCreateDto.deadline());
            throw new IllegalArgumentException("Deadline cannot be more than 2 years in the future");
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
    public Page<ProjectDto> getProjects(Pageable pageable) {
        Page<ProjectEntity> pageEntities = projectRepository.findAll(pageable);

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }

    @Override
    public Page<ProjectDto> getProjectsCurrentUser(Pageable pageable) {
        UserEntity user = UserUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not logged in"));

        Page<ProjectEntity> pageEntities = projectRepository.findAllByAuthorId(pageable, user.getId());

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }

    @Override
    @Transactional
    public void assignProjectToFreelancer(Long projectId, String freelancerId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        UserEntity user = userRepository.findById(freelancerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateProjectAssignment(project, user);

        validateFreelancerCapacity(user);

        project.setFreelancer(user);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setTakenAt(LocalDateTime.now());

        projectRepository.save(project);
        //todo: сделать уведомление для автора
    }

    @Override
    @Transactional
    public ProjectDto updateStatus(Long projectId, ProjectStatus status) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        if (project.getStatus() == ProjectStatus.CANCEL || project.getStatus() == ProjectStatus.COMPLETED) {
            log.info("Project status cancel or completed");
            throw new RuntimeException("Project status cancel or completed");
        }

        if (project.getStatus().equals(status)) {
            log.info("Project status already set");
            throw new RuntimeException("Project status already set");
        }

        if (!project.getAuthor().getId().equals(currentUser.getId())) {
            log.error("Current user is not the author");
            throw new RuntimeException("Current user is not the author");
        }

        project.setStatus(status);
        ProjectEntity savedProject = projectRepository.save(project);

        return projectMapper.mapProjectEntityToDto(savedProject);
    }

    private void validateFreelancerCapacity(UserEntity freelancer) {
        long activeProjectsCount = projectRepository
                .countByFreelancerIdAndStatus(freelancer.getId(), ProjectStatus.IN_PROGRESS);

        if (activeProjectsCount >= MAX_ACTIVE_PROJECTS) {
            throw new RuntimeException(
                    "Freelancer has reached maximum capacity of active projects");
        }
    }

    private void validateProjectAssignment(ProjectEntity project, UserEntity user) {
        if (project.getStatus() != ProjectStatus.PENDING) {
            log.error("The project has already been completed or cancelled");
            throw new RuntimeException("The project has already been completed or cancelled");
        }

        if (project.getFreelancer() != null) {
            log.error("The project has already been assigned to the freelancer");
            throw new RuntimeException("The project has already been assigned to the freelancer");
        }

        if (project.getAuthor().getId().equals(user.getId())) {
            log.error("Author cant take this project");
            throw new RuntimeException("Author cant take this project");
        }
    }
}
