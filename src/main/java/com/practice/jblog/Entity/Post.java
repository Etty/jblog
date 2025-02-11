package com.practice.jblog.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.practice.jblog.formatters.TimeStampDeserializer;
import com.practice.jblog.formatters.TimeStampSerializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(indexes = {
        @Index(name = "POST_TITLE", columnList = "title")
})
public class Post implements SearchableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("1")
    private byte isEnabled;

    @Column(unique = true, updatable = false)
    private String postIdentifier;

    @NotBlank(message = "Post title cannot be blank")
    private String title;

    private String image;

    @Lob
    private String description;

    @Column(unique = true)
    private String urlKey;

    @ManyToMany
    @JoinTable(
            name = "category_post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @JsonIgnore
    private Set<Category> categories;

    @JsonFormat(pattern = "yyyy-mm-dd")
    @CreationTimestamp
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonSerialize(using = TimeStampSerializer.class)
    @JsonDeserialize(using = TimeStampDeserializer.class)
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-mm-dd")
    @UpdateTimestamp
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @JsonSerialize(using = TimeStampSerializer.class)
    @JsonDeserialize(using = TimeStampDeserializer.class)
    private Instant updatedAt;

    @Transient
    List<String> categoryIds = null;

    public Long getId() {
        return id;
    }

    public Post setId(Long id) {
        this.id = id;
        return this;
    }

    public byte getIsEnabled() {
        return isEnabled;
    }

    public Post setIsEnabled(byte isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    public String getPostIdentifier() {
        return postIdentifier;
    }

    public Post setPostIdentifier(String postIdentifier) {
        this.postIdentifier = postIdentifier;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Post setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Post setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public Post setUrlKey(String urlKey) {
        this.urlKey = urlKey;
        return this;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public List<String> getCategoryIds() {
        if (categoryIds != null) {return categoryIds;}

        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        categoryIds = categories.stream()
                .map(Category::getCategoryIdentifier)
                .collect(Collectors.toList());

        return categoryIds;
    }

    public Post setCategories(Set<Category> categories) {
        this.categories = categories;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Post setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Post setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    @JsonIgnore
    public String getIdField() {
        return postIdentifier;
    }
}
