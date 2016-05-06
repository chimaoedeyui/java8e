package com.example.jk.java8e;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.trello.rxlifecycle.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle.ActivityEvent.PAUSE;

public class ScanActivity extends RxAppCompatActivity {

    public static final String EXTRA_CHARACTERISTIC_UUID = "extra_uuid";
    @Bind(R.id.scan_toggle_btn)
    Button scanToggleButton;
    @Bind(R.id.scan_results1)
    RecyclerView recyclerView1;
    @Bind(R.id.connection_state)
    TextView connectionState;

    @Bind(R.id.connect)
    Button connectButton;
    @Bind(R.id.read_output)
    TextView readOutputView;
    @Bind(R.id.read_hex_output)
    TextView readHexOutputView;
    @Bind(R.id.write_input)
    TextView writeInput;
    @Bind(R.id.read)
    Button readButton;
    @Bind(R.id.write)
    Button writeButton;
    @Bind(R.id.notify)
    Button notifyButton;
    @Bind(R.id.logtex)
    TextView logtex;
    @Bind(R.id.scan_results2)
    RecyclerView recyclerView2;
    private DiscoveryResultsAdapter adapter;

    private RxBleClient rxBleClient;
    private Subscription scanSubscription;
    private Subscription connectionSubscription;
    private ScanResultsAdapter resultsAdapter;
    private RxBleDevice bleDevice;

    private UUID characteristicUuid;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    String macAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example1);
        ButterKnife.bind(this);
        rxBleClient = SampleApplication.getRxBleClient(this);
        configureResultList1();
    }

    @OnClick(R.id.scan_toggle_btn)
    public void onScanToggleClick() {

        if (isScanning()) {
            scanSubscription.unsubscribe();
        } else {
            scanSubscription = rxBleClient.scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::clearSubscription1)
                    .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
        }

        updateButtonUIState();
    }

    @OnClick(R.id.connect)
    public void onConnectToggleClick() {

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
            /*
             * Stop scanning in onPause callback. You can use rxlifecycle for convenience. Examples are provided later.
             */
            scanSubscription.unsubscribe();
        }
    }

    private void configureResultList1() {
        recyclerView1.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView1.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ScanResultsAdapter();
        recyclerView1.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView1.getChildAdapterPosition(view);
            final RxBleScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick1(itemAtPosition);
        });
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    private void onAdapterItemClick1(RxBleScanResult scanResults) {

/*        scanSubscription.unsubscribe();
        recyclerView1.setVisibility(View.GONE);


        configureResultLis2t();
        macAddress = scanResults.getBleDevice().getMacAddress();
        bleDevice = SampleApplication.getRxBleClient(this).getBleDevice(macAddress);
        // How to listen for connection state changes
        bleDevice.observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);

        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionSubscription = bleDevice.establishConnection(this, false)
                    .flatMap(RxBleConnection::discoverServices)
                    .first() // Disconnect automatically after discovery
                    .compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::updateUI)
                    .subscribe(adapter::swapScanResult, this::onConnectionFailure);

        }
        updateUI();*/

    }

    private void configureResultLis2t() {
        recyclerView2.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(recyclerLayoutManager);
        adapter = new DiscoveryResultsAdapter();
        recyclerView2.setAdapter(adapter);
        adapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView2.getChildAdapterPosition(view);
            final DiscoveryResultsAdapter.AdapterItem itemAtPosition = adapter.getItem(childAdapterPosition);
            onAdapterItemClick2(itemAtPosition);
        });
    }

    private void onAdapterItemClick2(DiscoveryResultsAdapter.AdapterItem item) {

        if (item.type == DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC) {
            characteristicUuid =  item.uuid;
            connectionObservable = bleDevice
                    .establishConnection(this, false)
                    .takeUntil(disconnectTriggerSubject)
                    .compose(bindUntilEvent(PAUSE))
                    .doOnUnsubscribe(this::clearSubscription)
                    .compose(new ConnectionSharingAdapter());
        } else {
            //noinspection ConstantConditions
            Snackbar.make(findViewById(android.R.id.content), R.string.not_clickable, Snackbar.LENGTH_SHORT).show();
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
        scanToggleButton.setText(isScanning() ? R.string.stop_scan : R.string.start_scan);
    }


    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(android.R.id.content), "Connection error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }


    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        connectionState.setText(newState.toString());
        updateUI();
    }


    private void triggerDisconnect() {

        if (connectionSubscription != null) {
            connectionSubscription.unsubscribe();
        }
    }

    private void updateUI() {
        final boolean connected = isConnected();
        connectButton.setText(isConnected() ? getString(R.string.disconnect) : getString(R.string.connect));
        readButton.setEnabled(isConnected());
        writeButton.setEnabled(isConnected());
        notifyButton.setEnabled(isConnected());
    }
    private void clearSubscription() {
        connectionObservable = null;
        updateUI();
    }
}
