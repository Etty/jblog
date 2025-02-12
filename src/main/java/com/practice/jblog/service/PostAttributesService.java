package com.practice.jblog.service;

import com.practice.jblog.entity.PostAttributeDefinition;
import com.practice.jblog.repository.PostAttributeDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostAttributesService {
    private PostAttributeDefinitionRepository postAttributeDefinitionRepository;

    @Autowired
    public PostAttributesService(PostAttributeDefinitionRepository postAttributeDefinitionRepository) {
        this.postAttributeDefinitionRepository = postAttributeDefinitionRepository;
    }

    public List<PostAttributeDefinition> getSearchableAttributes() {
        return postAttributeDefinitionRepository.findAllByIsUsedInSearch((byte)1);
    }
}
