package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserLike> likes = new HashSet<>();

    private boolean notificationSetting = true;

    private LocalDateTime commentBanUntil; // 댓글 작성 정지 종료 시간

    private LocalDateTime profilePictureBanUntil; // 프로필 사진 변경 정지 종료 시간

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
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

    public LocalDateTime getCommentBanUntil() {
        if (commentBanUntil != null) {
            return commentBanUntil.withSecond(0).withNano(0);
        }
        return null;
    }

    public void setCommentBanUntil(LocalDateTime commentBanUntil) {
        this.commentBanUntil = commentBanUntil;
    }

    public LocalDateTime getProfilePictureBanUntil() {
        if (profilePictureBanUntil != null) {
            return profilePictureBanUntil.withSecond(0).withNano(0);
        }
        return null;
    }

    public void setProfilePictureBanUntil(LocalDateTime profilePictureBanUntil) {
        this.profilePictureBanUntil = profilePictureBanUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}

