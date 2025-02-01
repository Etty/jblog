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

    public void setId(Long id) {
        this.id = id;
    }

    public byte getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(byte isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getPostIdentifier() {
        return postIdentifier;
    }

    public void setPostIdentifier(String postIdentifier) {
        this.postIdentifier = postIdentifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
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

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    @JsonIgnore
    public String getIdField() {
        return postIdentifier;
    }
}
