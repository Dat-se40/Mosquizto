package com.example.mosquizto.Dto.response;

public class MediaSignResponse {

    private String cloudName;
    private String apiKey;
    private long timestamp;
    private String signature;
    private String folder;
    private String publicId;

    public MediaSignResponse() {
    }

    public MediaSignResponse(String cloudName, String apiKey, long timestamp, String signature, String folder, String publicId) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.timestamp = timestamp;
        this.signature = signature;
        this.folder = folder;
        this.publicId = publicId;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}
