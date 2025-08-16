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
import com.bolezni.security.CustomUserDetails;
import com.bolezni.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
        UserEntity author = getCurrentUser()
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

        UserEntity user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found or not logged in"));

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
        boolean changed = false;

        changed |= updateStringField(updateDto.title(), project.getTitle(), project::setTitle);
        changed |= updateStringField(updateDto.description(), project.getDescription(), project::setDescription);
        changed |= updatePriceField(updateDto.price(), project.getPrice(), project::setPrice);

        changed |= updateCategoriesField(project, updateDto.categories());

        return changed;
    }

    private boolean updateStringField(String newValue, String currentValue, Consumer<String> setter) {
        if (StringUtils.hasText(newValue)) {
            String trimmedValue = newValue.trim();
            if (!Objects.equals(trimmedValue, currentValue)) {
                setter.accept(trimmedValue);
                return true;
            }
        }
        return false;
    }

    private boolean updatePriceField(BigDecimal newPrice, BigDecimal currentPrice, Consumer<BigDecimal> setter) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) > 0 &&
                !Objects.equals(currentPrice, newPrice)) {
            setter.accept(newPrice);
            return true;
        }
        return false;
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
        UserEntity user = getCurrentUser()
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

    private Optional<UserEntity> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return Optional.of(((CustomUserDetails) principal).getUser());
            } else if (principal instanceof UserEntity) {
                return Optional.of((UserEntity) principal);
            }
        }
        return Optional.empty();
    }

    @Override
    public Page<ProjectDto> getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<ProjectEntity> pageEntities = projectRepository.findAll(pageable);

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }

    @Override
    public Page<ProjectDto> getProjectsCurrentUser(int page, int size, String userId) {
        if (userId == null)
            throw new IllegalArgumentException("User id is null");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));

        Page<ProjectEntity> pageEntities = projectRepository.findAllByAuthorId(pageable, userId);

        return pageEntities.map(projectMapper::mapProjectEntityToDto);
    }
}
