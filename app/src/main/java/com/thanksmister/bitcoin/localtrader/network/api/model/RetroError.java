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

package com.thanksmister.bitcoin.localtrader.network.api.model;

import java.lang.Error;

public class RetroError extends Error {
    private String message;
    private int code;

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public RetroError(String detailMessage) {
        super(detailMessage);
        this.message = detailMessage;
    }

    public RetroError(String detailMessage, int code) {
        super(detailMessage);
        this.message = detailMessage;
        this.code = code;
    }

    public boolean isAuthenticationError() {
        return (code == 403 || code == 4);
    }

    public boolean isNetworkError() {
        return (code == 404);
    }
}
