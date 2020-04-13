package com.querydsl.r2dbc.test.env.schema;

import javax.annotation.Generated;

/**
 * SUser is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class SUser {

    private java.time.LocalDateTime creationTime;

    private Boolean disabled;

    private Long id;

    private String passwordHash;

    private String passwordSalt;

    private String personName;

    private String preferredLocaleCountryCode;

    private String preferredLocaleLanguageCode;

    private java.util.UUID publicId;

    private Long roleChangeCount;

    private java.time.LocalDateTime roleChangeTime;

    private Long updateCount;

    private java.time.LocalDateTime updateTime;

    private String username;

    public java.time.LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(java.time.LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPreferredLocaleCountryCode() {
        return preferredLocaleCountryCode;
    }

    public void setPreferredLocaleCountryCode(String preferredLocaleCountryCode) {
        this.preferredLocaleCountryCode = preferredLocaleCountryCode;
    }

    public String getPreferredLocaleLanguageCode() {
        return preferredLocaleLanguageCode;
    }

    public void setPreferredLocaleLanguageCode(String preferredLocaleLanguageCode) {
        this.preferredLocaleLanguageCode = preferredLocaleLanguageCode;
    }

    public java.util.UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(java.util.UUID publicId) {
        this.publicId = publicId;
    }

}
