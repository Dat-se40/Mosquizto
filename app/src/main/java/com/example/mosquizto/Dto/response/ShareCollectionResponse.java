package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Util.AccessStatus;
import com.example.mosquizto.Util.CollectionRole;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.Date;

public class ShareCollectionResponse implements NotificationWrapper {
    Long inviterId ;
    String inviterUsername ;
    Integer collectionId ;
    String title ;
    String description ;
    String inviteAt ;
    CollectionRole collectionRole ;
    AccessStatus accessStatus ;

    public AccessStatus getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(AccessStatus accessStatus) {
        this.accessStatus = accessStatus;
    }

    public CollectionRole getCollectionRole() {
        return collectionRole;
    }

    public void setCollectionRole(CollectionRole collectionRole) {
        this.collectionRole = collectionRole;
    }

    public String getInviteAt() {
        return inviteAt;
    }

    public void setInviteAt(String inviteAt) {
        this.inviteAt = inviteAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public void setInviterUsername(String inviterUsername) {
        this.inviterUsername = inviterUsername;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    @Override
    public int getType() {
        return 1;
    }
}
