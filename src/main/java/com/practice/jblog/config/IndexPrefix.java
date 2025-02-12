package com.practice.jblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexPrefix {
    @Value("${elastic.index_prefix}")
    private String postsIndexName;

    public String getPrefix() {
        return postsIndexName;
    }
}
