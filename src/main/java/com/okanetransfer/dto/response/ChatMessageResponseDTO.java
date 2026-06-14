package com.okanetransfer.dto.response;

public class ChatMessageResponseDTO {

    private String reply;
    private boolean escalated;

    public ChatMessageResponseDTO() {
    }

    public ChatMessageResponseDTO(String reply, boolean escalated) {
        this.reply = reply;
        this.escalated = escalated;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }
}