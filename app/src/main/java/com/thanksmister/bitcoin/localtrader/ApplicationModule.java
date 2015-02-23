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
package com.thanksmister.bitcoin.localtrader;

import android.location.LocationManager;

import com.thanksmister.bitcoin.localtrader.data.DataModule;
import com.thanksmister.bitcoin.localtrader.domain.DomainModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.LOCATION_SERVICE;

@Module(
        injects = { BaseApplication.class},
        includes = {DataModule.class, DomainModule.class},
        library = true
)

public class ApplicationModule
{
    private final BaseApplication app;

    public ApplicationModule(BaseApplication app) 
    {
        this.app = app;
    }
    
    @Provides 
    BaseApplication provideApplication() 
    {
        return app;
    }
    
    /*@Provides
    @Singleton
    Gson provideGson()
    {
        return new GsonBuilder().create();
    }*/
 
    /*@Provides
    @Singleton
    Bus provideBus()
    {
        return new ApplicationBus();
    }*/

    @Provides 
    @Singleton
    LocationManager provideLocationManager()
    {
        return (LocationManager) app.getSystemService(LOCATION_SERVICE);
    }
}