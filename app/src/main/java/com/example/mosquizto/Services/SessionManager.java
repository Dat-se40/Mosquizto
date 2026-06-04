package com.example.mosquizto.Services;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.mosquizto.Models.User;
import com.google.gson.Gson;

import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {
    private static final String PREF_NAME = "MosquiztoSession";
    private static final String KEY_TOKEN = "access_token";

    private static final String KEY_LOGIN = "login_time";
    private static final String KEY_USER = "user_json";

    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String PREFIX_COLLECTION_COUNT = "COLLECTION_COUNT_";
    private static final String PREFIX_COLLECTION_AUTHOR = "COLLECTION_AUTHOR_";

    private static final String PREFIX_COLLECTION_TITLE = "COLLECTION_TITLE_";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String accessToken;
    private String refreshToken ;
    private User currUser;
    private Gson gson;
    private long loginTimeExpired = 3600; 

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.gson = new Gson();

        this.accessToken = sharedPreferences.getString(KEY_TOKEN, null);
        this.refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            this.currUser = gson.fromJson(userJson, User.class);
        }
    }

    public void saveSession(String token, User user, String refreshToken) {
        this.accessToken = token;
        this.currUser = user;
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_LOGIN, System.currentTimeMillis());
        editor.apply();
    }

    public void saveCollectionMetadata(int collectionId, int count, String author) {
        editor.putInt(PREFIX_COLLECTION_COUNT + collectionId, count);
        if (author != null) {
            editor.putString(PREFIX_COLLECTION_AUTHOR + collectionId, author);
        }
        editor.apply();
    }

    public int getCollectionCount(int collectionId) {
        return sharedPreferences.getInt(PREFIX_COLLECTION_COUNT + collectionId, 0);
    }

    public String getCollectionAuthor(int collectionId) {
        return sharedPreferences.getString(PREFIX_COLLECTION_AUTHOR + collectionId, null);
    }

    public String getAccessToken() { return accessToken; }
    public User getCurrUser() { return currUser; }

    public void logout() {
        this.accessToken = null;
        this.currUser = null;
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() { return accessToken != null; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public Boolean isLoginExpired() {
        return (System.currentTimeMillis() - sharedPreferences.getLong(KEY_LOGIN, 0) )*1000 > loginTimeExpired;
    }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setCurrUser(User currUser) { this.currUser = currUser; }

    public void saveCollectionCount(Integer id, int i) {
        editor.putInt(PREFIX_COLLECTION_COUNT + id, i);
        editor.apply();
    }

    public void saveCollectionTitle(Integer id, String title)
    {
        editor.putString(PREFIX_COLLECTION_TITLE + id, title);
        editor.apply();
    }

    public String getCollectionTitle(Integer id)
    {
        return sharedPreferences.getString(PREFIX_COLLECTION_TITLE + id, null);
    }
    public void clearSession() {
        // xóa token
        setAccessToken(null);
        setRefreshToken(null);
        // xóa user
        setCurrUser(null);
    }
}