package com.thanksmister.bitcoin.localtrader.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thanksmister.bitcoin.localtrader.BaseFragment;
import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.data.api.model.Contact;
import com.thanksmister.bitcoin.localtrader.data.api.model.DashboardType;
import com.thanksmister.bitcoin.localtrader.data.api.model.Exchange;
import com.thanksmister.bitcoin.localtrader.data.api.model.Method;
import com.thanksmister.bitcoin.localtrader.data.database.AdvertisementItem;
import com.thanksmister.bitcoin.localtrader.data.database.ContactItem;
import com.thanksmister.bitcoin.localtrader.data.database.DbManager;
import com.thanksmister.bitcoin.localtrader.data.database.ExchangeItem;
import com.thanksmister.bitcoin.localtrader.data.database.MethodItem;
import com.thanksmister.bitcoin.localtrader.events.NavigateEvent;
import com.thanksmister.bitcoin.localtrader.events.NetworkEvent;
import com.thanksmister.bitcoin.localtrader.ui.misc.DashboardAdvertisementAdapter;
import com.thanksmister.bitcoin.localtrader.ui.misc.DashboardContactAdapter;
import com.thanksmister.bitcoin.localtrader.ui.misc.LinearListView;
import com.thanksmister.bitcoin.localtrader.ui.misc.AdvertisementAdapter;
import com.thanksmister.bitcoin.localtrader.ui.misc.ContactAdapter;
import com.thanksmister.bitcoin.localtrader.utils.Calculations;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindFragment;

/**
 * Author: Michael Ritchie
 * Date: 12/30/14
 * Copyright 2013, ThanksMister LLC
 */
public class DashboardFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener
{
    public static final String EXTRA_METHODS = "com.thanksmister.extras.EXTRA_METHODS";
    private static final String ARG_SECTION_NUMBER = "section_number";
    
    @Inject
    DbManager dbManager;

    @Inject
    Bus bus;

    @InjectView(R.id.dashContent)
    View content; 
    
    @InjectView(android.R.id.progress)
    View progress;

    @InjectView(R.id.advertisementList)
    LinearListView advertisementList;

    @InjectView(R.id.contactList)
    LinearListView contactsList;

    @InjectView(R.id.bitcoinPrice)
    TextView bitcoinPrice;

    @InjectView(R.id.bitcoinValue)
    TextView bitcoinValue;

    @InjectView(R.id.tradesLayout)
    View tradesLayout;

    @InjectView(R.id.emptyAdvertisementsLayout)
    View emptyAdvertisementsLayout;

    @InjectView(R.id.emptyTradesLayout)
    View emptyTradesLayout;

    @InjectView(R.id.advertisementsLayout)
    View advertisementsLayout;

    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;

    @OnClick(R.id.emptyTradesLayout)
    public void emptyTradesButtonClicked()
    {
        showTradesScreen();
    }

    @OnClick(R.id.tradesButton)
    public void tradesButtonClicked()
    {
        showTradesScreen();
    }

    @OnClick(R.id.advertisementsButton)
    public void advertisementsButtonClicked()
    {
        createAdvertisementScreen();
    }

    @OnClick(R.id.emptyAdvertisementsLayout)
    public void emptyAdvertisementsButtonClicked()
    {
        createAdvertisementScreen();
    }

    @OnClick(R.id.dashboardFloatingButton)
    public void scanButtonClicked()
    {
        scanQrCode();
    }

    @Optional
    @OnClick(R.id.searchButton)
    public void searchButtonClicked()
    {
        showSearchScreen();
    }

    @Optional
    @OnClick(R.id.advertiseButton)
    public void advertiseButtonClicked()
    {
        createAdvertisementScreen();
    }

    private Observable<List<AdvertisementItem>> advertisementObservable;
    private Observable<List<MethodItem>> methodObservable;
    private Observable<List<ContactItem>> contactObservable;
    private Observable<ExchangeItem> exchangeObservable;

    private Observable<List<Contact>> contactUpdateObservable;
    private Observable<List<Advertisement>> advertisementUpdateObservable;
    private Observable<List<Method>> methodUpdateObservable;
    private Observable<Exchange> exchangeUpdateObservable;
  
    CompositeSubscription subscriptions = new CompositeSubscription();
    CompositeSubscription updateSubscriptions = new CompositeSubscription();

    private DashboardContactAdapter contactAdapter;
    private DashboardAdvertisementAdapter advertisementAdapter;

    
    private class AdvertisementData {
        public List<AdvertisementItem> advertisements;
        public List<MethodItem> methods;
    }

    private class dashboardData
    {
        public List<Advertisement> advertisements;
        public List<Contact> contacts;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DashboardFragment newInstance(int sectionNumber)
    {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment()
    {
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // database data
        methodObservable = bindFragment(this, dbManager.methodQuery().cache());
        contactObservable = bindFragment(this, dbManager.contactsQuery());
        advertisementObservable = bindFragment(this, dbManager.advertisementQuery());
        exchangeObservable = bindFragment(this, dbManager.exchangeQuery());
        
        // update data
        contactUpdateObservable = bindFragment(this, dbManager.getContacts(DashboardType.ACTIVE));
        advertisementUpdateObservable = bindFragment(this, dbManager.getAdvertisements());
        methodUpdateObservable = bindFragment(this, dbManager.getMethods());
        exchangeUpdateObservable = bindFragment(this, dbManager.getExchange());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.view_dashboard, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) 
    {
        super.onViewCreated(view, savedInstanceState);
        
        showProgress();

        subscribeData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.red));

        contactAdapter = new DashboardContactAdapter(getActivity());
        advertisementAdapter = new DashboardAdvertisementAdapter(getActivity());

        setAdvertisementAdapter(advertisementAdapter);
        setContactAdapter(contactAdapter);

        contactsList.setOnItemClickListener((adapterView, view, position, l) -> {
            ContactItem contact = (ContactItem) contactsList.getItemAtPosition(position);
            showContact(contact);
        });

        advertisementList.setOnItemClickListener((adapterView, view, position, l) -> {
            AdvertisementItem advertisement = (AdvertisementItem) advertisementList.getItemAtPosition(position);
            showAdvertisement(advertisement);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
        inflater.inflate(R.menu.dashboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_search:
                showSearchScreen();
                return false;
            case R.id.action_send:
                showSendScreen();
                return true;
            case R.id.action_logout:
                logOut();
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        updateData();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDetach()
    {
        ButterKnife.reset(this);

        subscriptions.unsubscribe();
        
        updateSubscriptions.unsubscribe();
        
        super.onDetach();
    }

    public Context getContext()
    {
        return getActivity();
    }

    @Override
    public void onRefresh()
    {
        updateData();
    }

    protected void onRefreshStart()
    {
        swipeLayout.setRefreshing(true);
    }

    protected void onRefreshStop()
    {
        hideProgress();
        swipeLayout.setRefreshing(false);
    }
    
    public void showProgress()
    {
        progress.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }

    public void hideProgress()
    {
        content.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }
    
    protected void subscribeData()
    {
        subscriptions.add(Observable.combineLatest(methodObservable, advertisementObservable, new Func2<List<MethodItem>, List<AdvertisementItem>, AdvertisementData>()
        {
            @Override
            public AdvertisementData call(List<MethodItem> methods, List<AdvertisementItem> advertisements)
            {
                AdvertisementData advertisementData = new AdvertisementData();
                advertisementData.methods = methods;
                advertisementData.advertisements = advertisements;
                return advertisementData;
            }
        }).subscribe(new Action1<AdvertisementData>()
        {
            @Override
            public void call(AdvertisementData advertisementData)
            {
                onRefreshStop();
                setAdvertisementList(advertisementData.advertisements, advertisementData.methods);
            }
        }));
        
        subscriptions.add(contactObservable.subscribe(new Action1<List<ContactItem>>()
        {
            @Override
            public void call(List<ContactItem> items)
            {
                Timber.d("Contacts Query: " + items.size());
                setContacts(items);
            }
        }));

        subscriptions.add(exchangeObservable.subscribe(new Action1<ExchangeItem>()
        {
            @Override
            public void call(ExchangeItem exchange)
            {
                if (exchange != null) {
                    String value = Calculations.calculateAverageBidAskFormatted(exchange.bid(), exchange.ask());
                    setMarketValue(exchange.exchange(), value);
                }
            }
        }));
    }
    
    protected void updateData()
    {
        onRefreshStart();
        
        updateSubscriptions.add(exchangeUpdateObservable.subscribe(new Action1<Exchange>()
        {
            @Override
            public void call(Exchange exchange)
            {
                Timber.e("Exchange Updated: " + exchange.name);

                dbManager.updateExchange(exchange);
            }
        }, new Action1<Throwable>()
        {
            @Override
            public void call(Throwable throwable)
            {
                Timber.e("Exchange Error: " + throwable.getLocalizedMessage());
                handleError(new Throwable("Error loading exchange data."));
            }
        }));
        
        updateSubscriptions.add(methodUpdateObservable.subscribe(new Action1<List<Method>>()
        {
            @Override
            public void call(List<Method> methods)
            {
                Timber.e("Methods Updated: " + methods.size());

                dbManager.updateMethods(methods);
            }
        }));
        
        subscriptions.add(Observable.combineLatest(contactUpdateObservable, advertisementUpdateObservable, new Func2<List<Contact>, List<Advertisement>, dashboardData>()
        {
            @Override
            public dashboardData call(List<Contact> contacts, List<Advertisement> advertisements)
            {
                dashboardData data = new dashboardData();
                data.contacts = contacts;
                data.advertisements = advertisements;
                return data;
            }
        }).subscribe(new Action1<dashboardData>()
        {
            @Override
            public void call(dashboardData data)
            {
                onRefreshStop();
                
                Timber.d("Contacts: " + data.contacts.size());
                Timber.d("Advertisements: " + data.advertisements.size());

                if (data.advertisements.size() > 0) {
                    dbManager.updateAdvertisements(data.advertisements);
                }

                if (data.contacts.size() > 0) {
                    dbManager.updateContacts(data.contacts);
                }
            }
        }, new Action1<Throwable>()
        {
            @Override
            public void call(Throwable throwable)
            {
                onRefreshStop();
                Timber.e("Desktop Errors: " + throwable.getLocalizedMessage());
                handleError(throwable);
            }
        }));
    }

    protected void setMarketValue(String exchange, String value)
    {
        if (bitcoinPrice != null) {
            bitcoinPrice.setText("$" + value + " / BTC");
            bitcoinValue.setText("Source " + exchange);
        }
    }

    protected void setContacts(List<ContactItem> data)
    {
        if(emptyTradesLayout != null) {
            emptyTradesLayout.setVisibility((data.size() == 0) ? View.VISIBLE : View.GONE);
            getContactAdapter().replaceWith(data);
        }  
    }

    protected void setAdvertisementList(List<AdvertisementItem> advertisements, List<MethodItem> methods)
    {
        if(emptyAdvertisementsLayout != null) {
            emptyAdvertisementsLayout.setVisibility((advertisements.size() == 0) ? View.VISIBLE : View.GONE);
            getAdvertisementAdapter().replaceWith(advertisements, methods);
        }
    }
    
    protected void setContactAdapter(DashboardContactAdapter adapter)
    {
        contactsList.setAdapter(adapter);
    }

    protected void setAdvertisementAdapter(DashboardAdvertisementAdapter adapter)
    {
        advertisementList.setAdapter(adapter);
    }

    protected ContactAdapter getContactAdapter()
    {
        return contactAdapter;
    }

    protected AdvertisementAdapter getAdvertisementAdapter()
    {
        return advertisementAdapter;
    }

    protected void showSendScreen()
    {
        bus.post(NavigateEvent.SEND);
    }

    protected void showSearchScreen()
    {
        bus.post(NavigateEvent.SEARCH);
    }

    public void scanQrCode()
    {
        bus.post(NavigateEvent.QRCODE);
    }

    protected void logOut()
    {
        bus.post(NavigateEvent.LOGOUT_CONFIRM);
    }

    protected void showTradesScreen()
    {
        Intent intent = ContactsActivity.createStartIntent(getContext(), DashboardType.RELEASED);
        intent.setClass(getContext(), ContactsActivity.class);
        getContext().startActivity(intent);
    }

    protected void showContact(ContactItem contact)
    {
        Intent intent = ContactActivity.createStartIntent(getContext(), contact.contact_id());
        intent.setClass(getContext(), ContactActivity.class);
        getContext().startActivity(intent);
    }
    
    protected void showAdvertisement(AdvertisementItem advertisement)
    {
        Intent intent = AdvertisementActivity.createStartIntent(getContext(), advertisement.ad_id());
        intent.setClass(getContext(), AdvertisementActivity.class);
        getContext().startActivity(intent);
    }

    protected void createAdvertisementScreen()
    {
        Intent intent = EditActivity.createStartIntent(getContext(), true, null);
        intent.setClass(getContext(), EditActivity.class);
        getContext().startActivity(intent);
    }

    @Subscribe
    public void onNetworkEvent(NetworkEvent event)
    {
        if(event == NetworkEvent.DISCONNECTED) {
            toast(R.string.error_no_internet);
        }
    }
}