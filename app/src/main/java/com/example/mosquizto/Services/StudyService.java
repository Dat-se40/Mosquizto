package com.example.mosquizto.Services;

import com.example.mosquizto.Dto.response.CollectionResponse;

public interface StudyService {
    public void StartStudySession(CollectionResponse collection);
    public void EndStudySession(CollectionResponse collection);
}
