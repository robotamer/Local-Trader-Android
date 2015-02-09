/*
 * Copyright (c) 2014. ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.bitcoin.localtrader.ui.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.Contact;
import com.thanksmister.bitcoin.localtrader.utils.Dates;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class ContactAdapter extends BaseAdapter
{
    protected List<Contact> contacts = Collections.emptyList();
    protected Context context;
    protected final LayoutInflater inflater;
    
    public ContactAdapter(Context context)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    @Override
    public int getCount()
    {
        return contacts.size();
    }

    @Override
    public Contact getItem(int position)
    {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public void replaceWith(List<Contact> data)
    {
        contacts = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.adapter_contact_list, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Contact contact = getItem(position);

        String type = "";
        switch (contact.advertisement.trade_type) {
            case LOCAL_BUY:
            case LOCAL_SELL:
                type = (contact.is_buying)? "Buying Locally":"Selling Locally";
                break;
            case ONLINE_BUY:
            case ONLINE_SELL:
                type = (contact.is_buying)? "Buying Online":"Selling Online";
                break;
        }

        String amount =  contact.amount + " " + contact.currency;
        String btc =  contact.amount_btc + context.getString(R.string.btc);
        String person = (contact.is_buying)? contact.seller.username:contact.buyer.username;
        String date = Dates.parseLocalDateStringAbbreviatedTime(contact.created_at);

        holder.tradeType.setText(type + " - " + amount);
        holder.tradeDetails.setText("With " + person + " (" + date + ")");
        holder.contactMessageCount.setText(String.valueOf(contact.messages.size()));
        
        return view;
    }

    protected static class ViewHolder
    {   
        @InjectView(R.id.tradeType) 
        public TextView tradeType;
        @InjectView(android.R.id.icon)
        public ImageView icon;
        @InjectView(R.id.tradeDetails)
        public TextView tradeDetails;
        @InjectView(R.id.contactMessageCount)
        public TextView contactMessageCount;
        
        @Optional
        @InjectView(R.id.contactButton)
        public Button contactButton;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}


