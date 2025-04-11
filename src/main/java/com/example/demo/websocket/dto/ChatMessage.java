package com.example.demo.websocket.dto;

public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String messageId;
    private String timestamp;
    private String action;

    public ChatMessage(String senderId, String recipientId, String content, String messageId, String timestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}