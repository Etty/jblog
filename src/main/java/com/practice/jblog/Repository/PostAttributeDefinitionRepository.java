package com.practice.jblog.Repository;

import com.practice.jblog.Entity.PostAttributeDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttributeDefinitionRepository extends CrudRepository<PostAttributeDefinition, Long> {
    List<PostAttributeDefinition> findAllByIsUsedInSearch(byte isUsedInSearch);
}
