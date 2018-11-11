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

package com.thanksmister.bitcoin.localtrader.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.thanksmister.bitcoin.localtrader.BuildConfig
import com.thanksmister.bitcoin.localtrader.R
import com.thanksmister.bitcoin.localtrader.network.api.model.Advertisement
import com.thanksmister.bitcoin.localtrader.network.api.model.Method
import com.thanksmister.bitcoin.localtrader.network.api.model.TradeType
import com.thanksmister.bitcoin.localtrader.network.api.model.TradeType.*
import com.thanksmister.bitcoin.localtrader.ui.BaseActivity
import com.thanksmister.bitcoin.localtrader.ui.viewmodels.AdvertisementsViewModel
import com.thanksmister.bitcoin.localtrader.utils.Dates
import com.thanksmister.bitcoin.localtrader.utils.TradeUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_advertiser.*
import timber.log.Timber
import javax.inject.Inject

class AdvertiserActivity : BaseActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var viewModel: AdvertisementsViewModel

    private val disposable = CompositeDisposable()
    private var adId: Int = 0
    private var advertisement: Advertisement? = null

    private inner class AdvertisementData {
        var advertisement: Advertisement? = null
        var method: Method? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.view_advertiser)
        /*adId = if (savedInstanceState == null) {
            intent.getIntExtra(EXTRA_AD_ID, 0)
        } else {
            savedInstanceState.getInt(EXTRA_AD_ID, 0)
        }*/
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = ""
        }
        requestButton.setOnClickListener {
            showTradeRequest(advertisement)
        }
        requestButton.isEnabled = false
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AdvertisementsViewModel::class.java)
        observeViewModel(viewModel)
    }

    /*public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_AD_ID, adId)
        super.onSaveInstanceState(outState)
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.advertiser, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_profile -> {
                showProfile(advertisement)
                return true
            }
            R.id.action_location -> {
                showAdvertisementOnMap(advertisement)
                return true
            }
            R.id.action_website -> {
                showPublicAdvertisement(advertisement)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun observeViewModel(viewModel: AdvertisementsViewModel) {
        viewModel.getAlertMessage().observe(this, Observer { message ->
            if (message != null) {
                dialogUtils.showAlertDialog(this@AdvertiserActivity, message)
            }
        })
        viewModel.getToastMessage().observe(this, Observer { message ->
            Toast.makeText(this@AdvertiserActivity, message, Toast.LENGTH_LONG).show()
        })
        viewModel.fetchAdvertisment(adId)
        disposable.add(
                viewModel.getAdvertisement(adId)
                        .zipWith(viewModel.getMethods(), BiFunction { advertisement: Advertisement, methods: List<Method> ->
                            val method = TradeUtils.getMethodForAdvertisement(advertisement, methods);
                            val data = AdvertisementData()
                            data.advertisement = advertisement
                            data.method = method
                            data
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( { data ->
                            if (data.advertisement != null && data.method != null) {
                                advertisement = data.advertisement
                                setTradeRequirements(advertisement)
                                setHeader(advertisement!!.tradeType)
                                setTradeRequirements(advertisement)
                                if (TradeUtils.isOnlineTrade(advertisement!!)) {
                                    setAdvertisement(advertisement, data.method)
                                } else {
                                    setAdvertisement(advertisement, null)
                                }
                                advertiserContent.visibility = View.VISIBLE
                            }
                        }, { error -> 
                            Timber.e("Advertiser error: $error")
                            runOnUiThread { 
                                dialogUtils.showAlertDialog(this@AdvertiserActivity, getString(R.string.error_title), 
                                        getString(R.string.toast_error_opening_advertisement), DialogInterface.OnClickListener { _, _ ->
                                    finish()
                                })
                            }
                            if (!BuildConfig.DEBUG) {
                                Crashlytics.setString("advertiser", adId.toString())
                                Crashlytics.logException(error)
                            }
                        }))
    }

    @Suppress("DEPRECATION")
    private fun setAdvertisement(advertisement: Advertisement?, method: Method?) {
        if(advertisement != null) {
            val location = advertisement.location
            val provider = TradeUtils.getPaymentMethod(this@AdvertiserActivity, advertisement, method)
            val tradeType = TradeType.valueOf(advertisement.tradeType)
            when (tradeType) {
                ONLINE_SELL -> noteTextAdvertiser.text = Html.fromHtml(getString(R.string.advertiser_notes_text_online, getString(R.string.text_sell).toLowerCase(), advertisement.currency, provider))
                LOCAL_SELL -> noteTextAdvertiser.text = Html.fromHtml(getString(R.string.advertiser_notes_text_locally, getString(R.string.text_sell).toLowerCase(), advertisement.currency, location))
                TradeType.ONLINE_BUY -> noteTextAdvertiser.text = Html.fromHtml(getString(R.string.advertiser_notes_text_online, getString(R.string.text_buy_your), advertisement.currency, provider))
                LOCAL_BUY -> noteTextAdvertiser.text = Html.fromHtml(getString(R.string.advertiser_notes_text_locally, getString(R.string.text_buy_your), advertisement.currency, location))
                else -> {
                    // na-da
                }
            }

            if (advertisement.isATM) {
                priceLayout.visibility = View.GONE
                priceLayoutDivider.visibility = View.GONE
                tradePrice.text = getString(R.string.text_atm)
                noteTextAdvertiser.text = Html.fromHtml(getString(R.string.advertiser_notes_text_atm, advertisement.currency, location))
            } else {
                tradePrice.text = getString(R.string.trade_price, advertisement.tempPrice, advertisement.currency)
            }

            traderName.text = advertisement.profile.username

            if (advertisement.isATM) {
                adTradeLimit.text = ""
            } else {
                if (advertisement.maxAmount != null && advertisement.minAmount != null) {
                    adTradeLimit.text = getString(R.string.trade_limit, advertisement.minAmount, advertisement.maxAmount, advertisement.currency)
                }
                if (advertisement.maxAmount == null && advertisement.minAmount != null) {
                    adTradeLimit.text = getString(R.string.trade_limit_min, advertisement.minAmount, advertisement.currency)
                }
                if (advertisement.maxAmountAvailable != null && advertisement.minAmount != null) { // no maximum set
                    adTradeLimit.text = getString(R.string.trade_limit, advertisement.minAmount, advertisement.maxAmountAvailable, advertisement.currency)
                } else if (advertisement.maxAmountAvailable != null) {
                    adTradeLimit.text = getString(R.string.trade_limit_max, advertisement.maxAmountAvailable, advertisement.currency)
                }
            }
            if (!TextUtils.isEmpty(advertisement.message)) {
                tradeTerms.text = advertisement.message!!.trim { it <= ' ' }
            }
            tradeFeedback.setText(advertisement.profile.feedbackScore)
            tradeCount.text = advertisement.profile.tradeCount
            if (advertisement.profile.lastOnline != null) {
                lastSeenIcon.setBackgroundResource(TradeUtils.determineLastSeenIcon(advertisement.profile.lastOnline!!))
            }
            val date = Dates.parseLocalDateStringAbbreviatedTime(advertisement.profile.lastOnline)
            dateText.text = getString(R.string.text_last_seen, date)
            requestButton.isEnabled = true
        }
    }

    private fun setTradeRequirements(advertisement: Advertisement?) {
        if(advertisement != null) {
            var showLayout = false
            if (advertisement.trustedRequired
                    || advertisement.smsVerificationRequired
                    || advertisement.requireIdentification) {
                showLayout = true
            }
            trustedTextView!!.visibility = if (advertisement.trustedRequired) View.VISIBLE else View.GONE
            identifiedTextView!!.visibility = if (advertisement.requireIdentification) View.VISIBLE else View.GONE
            smsTextView!!.visibility = if (advertisement.smsVerificationRequired) View.VISIBLE else View.GONE
            if (!TextUtils.isEmpty(advertisement.requireFeedbackScore) && TradeUtils.isOnlineTrade(advertisement)) {
                feedbackText!!.visibility = View.VISIBLE
                feedbackText!!.text = Html.fromHtml(getString(R.string.trade_request_minimum_feedback_score, advertisement.requireFeedbackScore))
                showLayout = true
            } else {
                feedbackText!!.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(advertisement.requireTradeVolume) && TradeUtils.isOnlineTrade(advertisement)) {
                volumeText!!.visibility = View.VISIBLE
                volumeText!!.text = Html.fromHtml(getString(R.string.trade_request_minimum_volume, advertisement.requireTradeVolume))
                showLayout = true
            } else {
                volumeText!!.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(advertisement.firstTimeLimitBtc) && TradeUtils.isOnlineTrade(advertisement)) {
                limitText!!.visibility = View.VISIBLE
                limitText!!.text = Html.fromHtml(getString(R.string.trade_request_new_buyer_limit, advertisement.firstTimeLimitBtc))
                showLayout = true
            } else {
                limitText!!.visibility = View.GONE
            }
            requirementsLayout!!.visibility = if (showLayout) View.VISIBLE else View.GONE
        }
    }

    private fun setHeader(tradeTypeString: String) {
        var header = ""
        val tradeType = TradeType.valueOf(tradeTypeString)
        header = when (tradeType) {
            ONLINE_SELL, TradeType.ONLINE_BUY -> if (tradeType == ONLINE_SELL) getString(R.string.text_online_seller) else getString(R.string.text_online_buyer)
            LOCAL_SELL, LOCAL_BUY -> if (tradeType == LOCAL_SELL) getString(R.string.text_local_seller) else getString(R.string.text_local_buyer)
            TradeType.NONE -> ""
        }
        if (supportActionBar != null) {
            supportActionBar!!.title = header
        }
    }

    private fun showTradeRequest(advertisement: Advertisement?) {
        if(advertisement != null) {
            val tradeType = TradeType.valueOf(advertisement.tradeType)
            if (TradeType.NONE.name == tradeType.name) {
                dialogUtils.showAlertDialog(this@AdvertiserActivity, getString(R.string.error_title), getString(R.string.error_invalid_trade_type),
                        DialogInterface.OnClickListener { dialog, which ->
                            if (!BuildConfig.DEBUG) {
                                Crashlytics.log("advertisement_data: " + advertisement.toString());
                                Crashlytics.logException(Throwable("Bad trade type for requested trade: $tradeType  $advertisement Id: $adId"))
                            }
                            finish()
                        })
                return
            }
            val intent = TradeRequestActivity.createStartIntent(this, advertisement.adId,
                    advertisement.tradeType, advertisement.countryCode, advertisement.onlineProvider,
                    advertisement.tempPrice, advertisement.minAmount,
                    advertisement.maxAmountAvailable, advertisement.currency,
                    advertisement.profile.username)
            startActivity(intent)
        }
    }

    private fun showPublicAdvertisement(advertisement: Advertisement?) {
        if(advertisement != null) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(advertisement.actions.publicView))
                startActivity(browserIntent)
            } catch (exception: ActivityNotFoundException) {
                toast(getString(R.string.toast_error_no_installed_ativity))
            }
        }
    }

    private fun showProfile(advertisement: Advertisement?) {
        if(advertisement != null) {
            val url = "https://localbitcoins.com/accounts/profile/" + advertisement.profile.username + "/"
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } catch (exception: ActivityNotFoundException) {
                toast(getString(R.string.toast_error_no_installed_ativity))
            }
        }
    }

    private fun showAdvertisementOnMap(advertisement: Advertisement?) {
        if(advertisement != null) {
            val geoUri = if (advertisement.tradeType == LOCAL_BUY.name || advertisement.tradeType == LOCAL_SELL.name) {
                "http://maps.google.com/maps?q=loc:" + advertisement.lat + "," + advertisement.lon + " (" + advertisement.location + ")"
            } else {
                "geo:0,0?q=" + advertisement.location!!
            }
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                startActivity(intent)
            } catch (exception: ActivityNotFoundException) {
                toast(getString(R.string.toast_error_no_installed_ativity))
            }
        }
    }

    companion object {
        const val EXTRA_AD_ID = "com.thanksmister.extras.EXTRA_AD_ID"
        fun createStartIntent(context: Context, adId: Int): Intent {
            val intent = Intent(context, AdvertiserActivity::class.java)
            intent.putExtra(EXTRA_AD_ID, adId)
            return intent
        }
    }
}