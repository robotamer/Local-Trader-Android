/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.thanksmister.bitcoin.localtrader.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thanksmister.bitcoin.localtrader.R;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public SpinnerAdapter(Context _context, int _resource, List<String> _items) {
        super(_context, _resource, _items);
        context = _context;
        items = _items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_transactions_header_layout, null, false);
        }

        TextView spinnerTarget = (TextView) convertView.findViewById(R.id.spinnerTarget);
        spinnerTarget.setText(items.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_layout, null, false);
        }

        TextView spinnerTarget = (TextView) convertView.findViewById(R.id.spinnerTarget);
        spinnerTarget.setText(items.get(position));

        return convertView;
    }
}
