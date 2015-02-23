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

package com.thanksmister.bitcoin.localtrader.data.api.transforms;

import com.thanksmister.bitcoin.localtrader.data.api.model.Currency;
import com.thanksmister.bitcoin.localtrader.utils.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import retrofit.client.Response;
import rx.functions.Func1;

public class ResponseToCurrencyList implements Func1<Response, List<Currency>>
{
    @Override
    public List<Currency> call(Response response)
    {
        //Try to get response body
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = sb.toString();
        List<Currency> currencies = Parser.parseCurrencies(result);
        return currencies;
    }
}