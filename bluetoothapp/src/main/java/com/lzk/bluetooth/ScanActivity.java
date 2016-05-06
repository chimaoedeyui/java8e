package com.lzk.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jk.bluetoothapp.MainActivity;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.exceptions.BleScanException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import com.example.jk.bluetoothapp.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.UUID;

public class ScanActivity extends RxAppCompatActivity {

    @Bind(R.id.connection_state)
    TextView connectionStateView;
    @Bind(R.id.scan_toggle_btn)
    Button scanToggleButton;
    @Bind(R.id.scan_results)
    RecyclerView recyclerView;
    private Subscription scanSubscription,connectionSubscription;;
    private ScanResultsAdapter resultsAdapter;
    private BLEManager bleManager;
    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scan);
        ButterKnife.bind(this);
        bleManager=BLEManager.getInstance(getApplication());
        configureResultList();
    }

    @OnClick(R.id.scan_toggle_btn)
    public void onScanToggleClick() {

        if (isScanning()) {
            scanSubscription.unsubscribe();
        } else {
            scanSubscription = bleManager.getRxBleClient().scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::clearSubscription1)
                    .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
        }

        updateButtonUIState();
    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(ScanActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(ScanActivity.this, "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(ScanActivity.this,
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(ScanActivity.this, "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(ScanActivity.this, "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            scanSubscription.unsubscribe();
        }
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ScanResultsAdapter();
        recyclerView.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final RxBleScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    private void onAdapterItemClick(RxBleScanResult scanResults) {
        // How to listen for connection state changes
        final String macAddress = scanResults.getBleDevice().getMacAddress();
        final Intent intent = new Intent(this, CharacteristicActivity.class);
        intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                String macAddress = data.getStringExtra(EXTRA_MAC_ADDRESS);
                String characteristicUuid = data.getStringExtra(CharacteristicActivity.EXTRA_CHARACTERISTIC_UUID);
                final Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
                intent.putExtra(CharacteristicActivity.EXTRA_CHARACTERISTIC_UUID, characteristicUuid);
                setResult(RESULT_OK, intent);
                Log.i("lzk","characteristicUuid :"+characteristicUuid );
                finish();//此处一定要调用finish()方法
                break;
            default:
                Log.i("lzk","not from character");
                break;
        }
    }

    private void onScanFailure(Throwable throwable) {

        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void clearSubscription1() {
        scanSubscription = null;
        resultsAdapter.clearScanResults();
        updateButtonUIState();
    }

    private void updateButtonUIState() {
        scanToggleButton.setText(isScanning() ? "stop scan" : "start scan");
    }

}
