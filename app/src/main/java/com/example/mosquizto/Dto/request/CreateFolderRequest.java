package com.example.mosquizto.Dto.request;

public class CreateFolderRequest {
    private String name;
    private String description;

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(String name, String description) {
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
    }

    public boolean isValid() {
        return name != null && !name.isEmpty()
                && description != null && !description.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
