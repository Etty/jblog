package com.practice.jblog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

/**
 * Properties of attributes, which can be managed by user
 */
@Table(indexes = {
        @Index(name = "POST_ATTRIBUTE_DEFINITION_IS_USED_IN_SEARCH", columnList = "is_used_in_search"),
        @Index(name = "POST_ATTRIBUTE_DEFINITION_IS_DISPLAYED_ON_POST_VIEW", columnList = "is_displayed_on_post_view")
})
@Entity
public class PostAttributeDefinition implements SearchableAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false)
    private String code;

    private String label;

    private String inputType;

    private String accept;

    @ColumnDefault("0")
    private byte isRequired;

    @ColumnDefault("0")
    private byte isUsedInSearch;

    /**
     * Then more number, then higher priority in search
     */
    @ColumnDefault("1")
    private int searchWeight;

    @ColumnDefault("0")
    private byte isWysiwygEnabled;

    private String frontendClass;

    @ColumnDefault("1000")
    private float sortOrder;

    @ColumnDefault("0")
    private byte isDisplayedOnPostView;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public PostAttributeDefinition setCode(String code) {
        this.code = code;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public byte getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(byte isRequired) {
        this.isRequired = isRequired;
    }

    public byte getIsUsedInSearch() {
        return isUsedInSearch;
    }

    public void setIsUsedInSearch(byte isUsedInSearch) {
        this.isUsedInSearch = isUsedInSearch;
    }

    public int getSearchWeight() {
        return searchWeight;
    }

    public void setSearchWeight(int searchWeight) {
        this.searchWeight = searchWeight;
    }

    public byte getIsWysiwygEnabled() {
        return isWysiwygEnabled;
    }

    public void setIsWysiwygEnabled(byte isWysiwygEnabled) {
        this.isWysiwygEnabled = isWysiwygEnabled;
    }

    public String getFrontendClass() {
        return frontendClass;
    }

    public void setFrontendClass(String frontendClass) {
        this.frontendClass = frontendClass;
    }

    public float getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(float sortOrder) {
        this.sortOrder = sortOrder;
    }

    public byte getIsDisplayedOnPostView() {
        return isDisplayedOnPostView;
    }

    public void setIsDisplayedOnPostView(byte isDisplayedOnPostView) {
        this.isDisplayedOnPostView = isDisplayedOnPostView;
    }
}
