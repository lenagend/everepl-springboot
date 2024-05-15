package com.everepl.evereplspringboot.dto;

public class UserRequest {
    private String name;
    private String imageUrl;
    private Boolean notificationSetting;

    // 기본 생성자
    public UserRequest() {
    }

    // 모든 필드를 인자로 받는 생성자
    public UserRequest(String name, String imageUrl, Boolean notificationSetting) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.notificationSetting = notificationSetting;
    }

    // Getter와 Setter 메소드
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // imageUrl을 추후에 설정할 수 있도록 setter 추가
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getNotificationSetting() {
        return notificationSetting;
    }

    public void setNotificationSetting(Boolean notificationSetting) {
        this.notificationSetting = notificationSetting;
    }
}
