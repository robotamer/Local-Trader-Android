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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.data.api.model.AdvertisementType;
import com.thanksmister.bitcoin.localtrader.data.api.model.Method;
import com.thanksmister.bitcoin.localtrader.utils.TradeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class AdvertisementAdapter extends BaseAdapter implements Filterable
{
    protected List<Advertisement> data = Collections.emptyList();
    protected List<Method> methods = Collections.emptyList();
    protected Context context;
    protected final LayoutInflater inflater;
    private ValueFilter valueFilter;
    private List<Advertisement> dataFiltered;

    public AdvertisementAdapter(Context context)
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
        return data.size();
    }

    @Override
    public Advertisement getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public void replaceWith(List<Advertisement> data, List<Method> methods)
    {
        this.data = data;
        this.methods = methods;
        dataFiltered =  data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.adapter_advertisement_list, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Advertisement advertisement = getItem(position);

        if (advertisement.visible)
            holder.row.setBackgroundColor(context.getResources().getColor(R.color.white));
        else
            holder.row.setBackgroundColor(context.getResources().getColor(R.color.list_gray_color));

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
        
        /*holder.tradePrice.setText(advertisement.price + " " + advertisement.currency);
        if(advertisement.max_amount == null) {
            holder.tradeLimit.setText(context.getString(R.string.trade_limit_min, advertisement.min_amount, advertisement.currency));
        } else { // no maximum set
            holder.tradeLimit.setText(context.getString(R.string.trade_limit, advertisement.min_amount, advertisement.max_amount));
        }*/

        String price = advertisement.price + " " + advertisement.currency;
        String location = advertisement.location;

        if (TradeUtils.isLocalTrade(advertisement)) {
            //holder.tradeType.setText(Html.fromHtml(context.getString(R.string.advertisement_list_text_local, type, price, location)));

            if(advertisement.isATM()) {
                holder.advertisementType.setText("ATM");
                holder.advertisementDetails.setText("ATM" + " in " + advertisement.city);
            } else {
                holder.advertisementType.setText(type + " " + price);
                holder.advertisementDetails.setText("In " + location);
            }

        } else {
            String paymentMethod = TradeUtils.getPaymentMethod(advertisement, methods);
            //String bank = advertisement.bank_name;
            holder.advertisementType.setText(type + " " + price);
            holder.advertisementDetails.setText("With " + paymentMethod + " in " + advertisement.city);
        }

        if (advertisement.visible) {
            holder.icon.setImageResource(R.drawable.ic_action_visibility);
        } else {
            holder.icon.setImageResource(R.drawable.ic_action_visibility_off);
        }

        return view;
    }

    @Override
    public Filter getFilter()
    {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }

        return valueFilter;
    }

    private class ValueFilter extends Filter
    {
        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();
            
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Advertisement> filterList = new ArrayList<Advertisement>();
                
                for (Advertisement advertisement : dataFiltered) {
                    
                    if (constraint.equals(AdvertisementType.INACTIVE.name())) {
                        
                        if(!advertisement.visible) {
                            filterList.add(advertisement);
                        }
                        
                    } else if (constraint.equals(AdvertisementType.ACTIVE.name())) {

                        if(advertisement.visible) {
                            filterList.add(advertisement);
                        }
  
                    } else {
                        filterList.add(advertisement);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = dataFiltered.size();
                results.values = dataFiltered;
            }
            return results;
        }

        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            data = (ArrayList<Advertisement>) results.values;
            notifyDataSetChanged();
        }
    }

    protected static class ViewHolder
    {
        @InjectView(android.R.id.background)
        public View row;
        @InjectView(R.id.advertisementType)
        public TextView advertisementType;
        @InjectView(android.R.id.icon)
        public ImageView icon;
        @InjectView(R.id.advertisementDetails)
        public TextView advertisementDetails;

        @Optional
        @InjectView(R.id.advertiseButton)
        public Button advertiseButton;

        public ViewHolder(View view)
        {
            ButterKnife.inject(this, view);
        }
    }
}


