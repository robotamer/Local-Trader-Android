/*
 * Copyright (c) 2015 ThanksMister LLC
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
 */

package com.thanksmister.bitcoin.localtrader.utils;

import junit.framework.TestCase;

/**
 * Author: Michael Ritchie
 * Updated: 12/19/15
 */
public class WalletUtilsTest extends TestCase
{
    public void testValidAmount() throws Exception
    {
        boolean valid = WalletUtils.validAmount("1");
        assertEquals(true, valid);
    }

    /*public void testFormatCurrencyAmountLeadingZeros() throws Exception
    {
        String value = "0010.10115014";
        String expected = "10.10";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountTrailingDecimalPlaces() throws Exception
    {
        String value = "1.10115014";
        String expected = "1.10";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountPeriodAtEnd() throws Exception
    {
        String value = "1.10115014.";
        String expected = "0.00";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountMultipleDecimals() throws Exception
    {
        String value = "0.010.10115014";
        String expected = "0.00";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountJustPeriod() throws Exception
    {
        String value = ".";
        String expected = "0.00";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountEmptyString() throws Exception
    {
        String value = "";
        String expected = "0.00";
        String response = Utils.formatCurrencyAmount(value, 2, 2);
        assertEquals(expected, response);
    }

    public void testFormatCurrencyAmountFromString() throws Exception
    {
        String value = "100.56";
        String expected = "100.56";
        String response = Utils.formatCurrencyAmount(value);
        assertEquals(expected, response);
    }

    public void testFormatBitcoinAmountFromString() throws Exception
    {
        String value = "100.56";
        String expected = "100.56";
        String response = Utils.formatCurrencyAmount(value);
        assertEquals(expected, response);
    }

    public void testConvertToCentsFromString() throws Exception
    {
        String value = "223";
        String expected = "2.23";
        String response = Utils.convertToCents(value);
        assertEquals(expected, response);
    }*/
}