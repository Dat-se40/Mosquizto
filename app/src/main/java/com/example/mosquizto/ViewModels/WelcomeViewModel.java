package com.example.mosquizto.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WelcomeViewModel extends ViewModel {

    private final MutableLiveData<String> _navigateTo = new MutableLiveData<>();
    public LiveData<String> navigateTo = _navigateTo;
    @Inject
    public WelcomeViewModel() {
    }
    public void onGoogleClicked() {
        _navigateTo.setValue("register");
    }

    public void onEmailClicked() {
        _navigateTo.setValue("register");
    }

    public void onLoginClicked() {
        _navigateTo.setValue("login");
    }

    public void onNavigationDone() {
        _navigateTo.setValue(null);
    }
    public void onWelcomeBackClicked()
    {

    }
}