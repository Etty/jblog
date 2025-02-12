package com.practice.jblog.repository;

import com.practice.jblog.entity.PostAttributeDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttributeDefinitionRepository extends CrudRepository<PostAttributeDefinition, Long> {
    List<PostAttributeDefinition> findAllByIsUsedInSearch(byte isUsedInSearch);
}
