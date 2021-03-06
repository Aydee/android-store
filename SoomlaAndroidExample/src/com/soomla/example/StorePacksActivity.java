package com.soomla.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreController;
import com.soomla.store.data.StorageManager;
import com.soomla.store.data.StoreInfo;
import com.soomla.store.domain.data.NonConsumableItem;
import com.soomla.store.domain.data.VirtualCurrencyPack;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

public class StorePacksActivity extends Activity {

    private StoreAdapter mStoreAdapter;
    private HashMap<String, Object> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        LinearLayout getMore = (LinearLayout)findViewById(R.id.getMore);
        TextView title = (TextView)findViewById(R.id.title);

        getMore.setVisibility(View.INVISIBLE);
        title.setText("Virtual Currency Packs");

        mImages = generateImagesHash();

        mStoreAdapter = new StoreAdapter();


        /* configuring the list with an adapter */

        final Activity activity = this;
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mStoreAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               /*
                * the user decided to make and actual purchase of virtual goods. we try to purchase and
                * StoreController tells us if the user has enough funds to make the purchase. If he won't
                * have enough than an InsufficientFundsException will be thrown.
                */

                if (i == 0) {
                    NonConsumableItem non = StoreInfo.getNonConsumableItems().get(0);
                    try {
                        StoreController.getInstance().buyGoogleMarketItem(non.getProductId());
                    } catch (VirtualItemNotFoundException e) {
                        AlertDialog ad = new AlertDialog.Builder(activity).create();
                        ad.setCancelable(false); // This blocks the 'BACK' button
                        ad.setMessage("Can't continue with purchase (the given product id did not match any actual product... Fix IStoreAssets)");
                        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ad.show();
                    }
                } else {
                    VirtualCurrencyPack pack = StoreInfo.getCurrencyPacks().get(i-1);
                    try {
                        StoreController.getInstance().buyGoogleMarketItem(pack.getProductId());
                    } catch (VirtualItemNotFoundException e) {
                        AlertDialog ad = new AlertDialog.Builder(activity).create();
                        ad.setCancelable(false); // This blocks the 'BACK' button
                        ad.setMessage("Can't continue with purchase (the given product id did not match any actual product... Fix IStoreAssets)");
                        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ad.show();

                    }
                }

                /* fetching the currency balance and placing it in the balance label */
                TextView muffinsBalance = (TextView)activity.findViewById(R.id.balance);
                muffinsBalance.setText("" + StorageManager.getVirtualCurrencyStorage().
                        getBalance(StoreInfo.getVirtualCurrencies().get(0)));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);

        /* fetching the currency balance and placing it in the balance label */
        TextView muffinsBalance = (TextView)findViewById(R.id.balance);
        muffinsBalance.setText("" + StorageManager.getVirtualCurrencyStorage().
                getBalance(StoreInfo.getVirtualCurrencies().get(0)));
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);
    }

    private HashMap<String, Object> generateImagesHash() {
        final HashMap<String, Object> images = new HashMap<String, Object>();

        images.put(MuffinRushAssets.NO_ADDS_NONCONS_PRODUCT_ID, R.drawable.no_ads);
        images.put(MuffinRushAssets.TENMUFF_PACK_PRODUCT_ID, R.drawable.muffins01);
        images.put(MuffinRushAssets.FIFTYMUFF_PACK_PRODUCT_ID, R.drawable.muffins02);
        images.put(MuffinRushAssets.FORTYMUFF_PACK_PRODUCT_ID, R.drawable.muffins03);
        images.put(MuffinRushAssets.THOUSANDMUFF_PACK_PRODUCT_ID, R.drawable.muffins04);

        return images;
    }

    @Subscribe
    public void onCurrencyBalanceChanged(CurrencyBalanceChangedEvent currencyBalanceChangedEvent) {
        /* fetching the currency balance and placing it in the balance label */
        TextView muffinsBalance = (TextView)findViewById(R.id.balance);
        muffinsBalance.setText("" + currencyBalanceChangedEvent.getBalance());
    }

    private class StoreAdapter extends BaseAdapter {

        public StoreAdapter() {
        }

        public int getCount() {
            return mImages.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if(convertView == null){
                vi = getLayoutInflater().inflate(R.layout.list_item, null);
            }

            TextView title = (TextView)vi.findViewById(R.id.title);
            TextView content = (TextView)vi.findViewById(R.id.content);
            TextView info = (TextView)vi.findViewById(R.id.item_info);
            ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image);

            // Setting all values in listview
            if (position == 0) {
                NonConsumableItem nonConsumableItem = StoreInfo.getNonConsumableItems().get(0);
                title.setText(nonConsumableItem.getName());
                content.setText(nonConsumableItem.getDescription());
                info.setText("");
                thumb_image.setImageResource((Integer)mImages.get(nonConsumableItem.getProductId()));
            } else {
                VirtualCurrencyPack pack = StoreInfo.getCurrencyPacks().get(position-1);
                title.setText(pack.getName());
                content.setText(pack.getDescription());
                info.setText("price: $" + pack.getPrice());
                thumb_image.setImageResource((Integer)mImages.get(pack.getProductId()));
            }

            return vi;
        }
    }

}