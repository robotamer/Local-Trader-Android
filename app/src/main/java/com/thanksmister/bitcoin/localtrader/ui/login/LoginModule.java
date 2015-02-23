package com.thanksmister.bitcoin.localtrader.ui.login;

/**
 * Author: Michael Ritchie
 * Date: 12/30/14
 * Copyright 2013, ThanksMister LLC
 */

import com.thanksmister.bitcoin.localtrader.ApplicationModule;
import com.thanksmister.bitcoin.localtrader.BaseApplication;
import com.thanksmister.bitcoin.localtrader.data.services.DataService;
import com.thanksmister.bitcoin.localtrader.ui.main.MainActivity;
import com.thanksmister.bitcoin.localtrader.ui.main.MainPresenter;
import com.thanksmister.bitcoin.localtrader.ui.main.MainPresenterImpl;
import com.thanksmister.bitcoin.localtrader.ui.main.MainView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * This module represents objects which exist only for the scope of a single activity. We can
 * safely create singletons using the activity instance because the entire object graph will only
 * ever exist inside of that activity.
 */
@Module(
        injects = {LoginActivity.class},
        addsTo = ApplicationModule.class
)
public class LoginModule
{
    private LoginView view;

    public LoginModule(LoginView view) 
    {
        this.view = view;
    }

    @Provides 
    @Singleton 
    public LoginView provideView() {
        return view;
    }

    @Provides @Singleton
    public LoginPresenter providePresenter(LoginView view, DataService dataService, BaseApplication application) 
    {
        return new LoginPresenterImpl(view, dataService, application);
    }
}