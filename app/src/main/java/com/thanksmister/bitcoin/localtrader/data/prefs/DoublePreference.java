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

package com.thanksmister.bitcoin.localtrader.data.prefs;

import android.content.SharedPreferences;

public class DoublePreference
{
    private final SharedPreferences preferences;
    private final String key;
    private final double defaultValue;

    public DoublePreference(SharedPreferences preferences, String key)
    {
        this(preferences, key, 0);
    }

    public DoublePreference(SharedPreferences preferences, String key, double defaultValue)
    {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public double get()
    {
        return Double.longBitsToDouble((preferences.getLong(key, Double.doubleToLongBits(defaultValue))));
    }

    public boolean isSet()
    {
        return preferences.contains(key);
    }

    //edit.putLong(key, Double.doubleToRawLongBits(value))
    public void set(double value)
    {
        preferences.edit().putLong(key, Double.doubleToRawLongBits(value)).commit();
    }

    public void delete()
    {
        preferences.edit().remove(key).commit();
    }
}