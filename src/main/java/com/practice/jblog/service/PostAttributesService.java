package com.practice.jblog.service;

import com.practice.jblog.Entity.PostAttributeDefinition;
import com.practice.jblog.Repository.PostAttributeDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostAttributesService {
    @Autowired
    private PostAttributeDefinitionRepository postAttributeDefinitionRepository;

    public List<PostAttributeDefinition> getSearchableAttributes() {
        return postAttributeDefinitionRepository.findAllByIsUsedInSearch((byte)1);
    }
}
