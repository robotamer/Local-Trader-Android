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

package com.thanksmister.bitcoin.localtrader.ui.dashboard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.data.api.model.TradeType;
import com.thanksmister.bitcoin.localtrader.ui.misc.AdvertisementAdapter;

public class DashboardAdvertisementAdapter extends AdvertisementAdapter
{
    public DashboardAdvertisementAdapter(Context context)
    {
        super(context);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.adapter_dashboard_advertisement_list, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Advertisement advertisement = getItem(position);
        String type = "";
        switch (advertisement.trade_type) {
            case LOCAL_BUY:
                type = "Local Buy - ";
                break;
            case LOCAL_SELL:
                type = "Local Sale -";
                break;
            case ONLINE_BUY:
                type = "Online Buy - ";
                break;
            case ONLINE_SELL:
                type = "Online Sale - ";
                break;
        }
        
        String price = advertisement.price + " " + advertisement.currency;
        String location = advertisement.location;

        if(advertisement.trade_type == TradeType.LOCAL_SELL || advertisement.trade_type == TradeType.LOCAL_BUY) {
            
            if(advertisement.isATM()) {
                holder.advertisementType.setText("ATM");
            } else {
                holder.advertisementType.setText(type + " " + price);
                holder.advertisementDetails.setText("In " + location);
            }
            
        } else {
            String bank = advertisement.bank_name;
            holder.advertisementType.setText(type + " " + price);
            holder.advertisementDetails.setText("With " +  bank + " in " + advertisement.city);
        }

        if (advertisement.visible) {
            holder.icon.setImageResource(R.drawable.ic_action_visibility_dark);
        } else {
            holder.icon.setImageResource(R.drawable.ic_action_visibility_off_dark);
        }

        holder.advertiseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((LinearListView) parent).performItemClick(v, position, 0);
            }
        });
        //holder.dateText.setText(Dates.parseLocalDateStringAbbreviatedDate(advertisement.created_at)); 
        return view;
    }
}


