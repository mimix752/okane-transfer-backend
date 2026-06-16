package com.okanetransfer.dto.request;

public class NotificationPreferencesDTO {
    private boolean notifyEmail;
    private boolean notifySms;

    public NotificationPreferencesDTO() {}

    public NotificationPreferencesDTO(boolean notifyEmail, boolean notifySms, boolean notifyPush) {
        this.notifyEmail = notifyEmail;
        this.notifySms = notifySms;
    }

    public NotificationPreferencesDTO(boolean notifyEmail, boolean notifySms) {
    }

    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public boolean isNotifySms() { return notifySms; }
    public void setNotifySms(boolean notifySms) { this.notifySms = notifySms; }



}