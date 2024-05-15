package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String imageUrl;

    // OAuth2 제공자로부터 받은 고유 ID

    private String providerId;

    // 로그인에 사용한 서비스 구분 (예: google, kakao, naver)
    private String provider;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserLike> likes = new HashSet<>();

    private boolean notificationSetting = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDisplayName() {
        return (name != null && !name.isEmpty()) ? name : "유저" + id;
    }

    public Set<UserLike> getLikes() {
        return likes;
    }

    public void setLikes(Set<UserLike> likes) {
        this.likes = likes;
    }

    public boolean isNotificationSetting() {
        return notificationSetting;
    }

    public void setNotificationSetting(boolean notificationSetting) {
        this.notificationSetting = notificationSetting;
    }
}