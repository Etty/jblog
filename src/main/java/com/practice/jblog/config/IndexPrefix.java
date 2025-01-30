package com.practice.jblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexPrefix {
    @Value("${elastic.index_prefix}")
    private String posts_index_name;

    public String getPrefix() {
        return posts_index_name;
    }
}
