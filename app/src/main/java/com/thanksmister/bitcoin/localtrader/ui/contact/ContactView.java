package com.thanksmister.bitcoin.localtrader.ui.contact;

import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.thanksmister.bitcoin.localtrader.data.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.data.api.model.Contact;
import com.thanksmister.bitcoin.localtrader.data.api.model.Message;
import com.thanksmister.bitcoin.localtrader.data.api.model.Method;

/**
 * Author: Michael Ritchie
 * Date: 12/30/14
 * Copyright 2013, ThanksMister LLC
 */
public interface ContactView
{
    void setToolBarMenu(Toolbar toolbar);

    Context getContext();

    public void onError(String message);

    public void onRefreshStop();
    
    public void showProgress();

    public void hideProgress();

    void setContact(Contact contact);

    void clearMessage();

    void downloadAttachment(Message message, String token);
}