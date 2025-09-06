package com.bolezni.mapper;

import com.bolezni.dto.ProjectCreateDto;
import com.bolezni.dto.ProjectDto;
import com.bolezni.model.CategoriesEntity;
import com.bolezni.model.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(target = "categories", qualifiedByName = "mapCategoriesToStrings")
    ProjectDto mapProjectEntityToDto(ProjectEntity projectEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "freelancer", ignore = true)
    @Mapping(target = "takenAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    ProjectEntity mapProjectCreateToProjectDto(ProjectCreateDto projectCreateDto);

    @Named("mapCategoriesToStrings")
    default Set<String> mapCategoriesToStrings(Set<CategoriesEntity> categories) {
        if (categories == null) {
            return Collections.emptySet();
        }
        return categories.stream()
                .map(CategoriesEntity::getName)
                .collect(Collectors.toSet());
    }

    @Named("mapStringsToCategories")
    default Set<CategoriesEntity> mapStringsToCategories(Set<String> categoryNames) {
        if (categoryNames == null) {
            return Collections.emptySet();
        }
        return categoryNames.stream()
                .map(name -> {
                    CategoriesEntity category = new CategoriesEntity();
                    category.setName(name);
                    return category;
                })
                .collect(Collectors.toSet());
    }
}
