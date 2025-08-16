package com.bolezni.repository;

import com.bolezni.model.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<CategoriesEntity, Long> {
    Set<CategoriesEntity> findByNameIn(Set<String> categories);

    Optional<CategoriesEntity> findByName(String name);
}
