package com.practice.jblog.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(indexes = {
        @Index(name = "CATEGORY_TITLE", columnList = "title"),
        @Index(name = "CATEGORY_PARENT_ID", columnList = "parent_id")
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("1")
    private byte isEnabled;

    @Column(unique = true, updatable = false)
    private String categoryIdentifier;

    @NotBlank(message = "Category title cannot be blank")
    private String title;

    private String image;

    @Lob
    private String description;

    @Column(unique = true)
    private String urlKey;

    private String categoryPath;

    /**
     * ID of parent category
     */
    @ColumnDefault("0")
    private long parentId;

    @ManyToMany(mappedBy = "categories")
    private Set<Post> posts;

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

    public Long getId() {
        return id;
    }

    public Category setId(Long id) {
        this.id = id;
        return this;
    }

    public byte getIsEnabled() {
        return isEnabled;
    }

    public Category setIsEnabled(byte isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    public String getCategoryIdentifier() {
        return categoryIdentifier;
    }

    public Category setCategoryIdentifier(String categoryIdentifier) {
        this.categoryIdentifier = categoryIdentifier;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Category setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Category setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Category setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public Category setUrlKey(String urlKey) {
        this.urlKey = urlKey;
        return this;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public Category setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
        return this;
    }

    public long getParentId() {
        return parentId;
    }

    public Category setParentId(long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Set<Post> getPosts() {
        return new HashSet<>(posts);
    }

    public Category setPosts(Set<Post> posts) {
        this.posts = posts;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Category setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Category setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
