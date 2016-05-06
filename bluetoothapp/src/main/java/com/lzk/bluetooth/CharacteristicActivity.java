package com.lzk.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.jk.bluetoothapp.R;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.trello.rxlifecycle.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle.ActivityEvent.PAUSE;

public class CharacteristicActivity extends RxAppCompatActivity {

    public static final String EXTRA_CHARACTERISTIC_UUID = "extra_uuid";

    @Bind(R.id.connect)
    Button connectButton;
    @Bind(R.id.scan_results)
    RecyclerView recyclerView;
    @Bind(R.id.connection_state)
    TextView connectionState;
    @Bind(R.id.failure)
    TextView failure;


    private DiscoveryResultsAdapter adapter;
    private RxBleDevice bleDevice;
    private String macAddress;
    private BLEManager bleManager;

    @OnClick(R.id.connect)
    public void onConnectToggleClick() {
        bleDevice.establishConnection(this, false)
                .flatMap(RxBleConnection::discoverServices)
              //  .first() // Disconnect automatically after discovery
                .compose(bindUntilEvent(PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::updateUI)
                .subscribe(adapter::swapScanResult, this::onConnectionFailure);

        updateUI();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic);
        ButterKnife.bind(this);
        macAddress = getIntent().getStringExtra(ScanActivity.EXTRA_MAC_ADDRESS);
        //noinspection ConstantConditions
      //  getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
        bleManager = BLEManager.getInstance(getApplication());

        bleDevice = bleManager.getRxBleClient().getBleDevice(macAddress);
        // How to listen for connection state changes
        bleDevice.observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
        configureResultList();
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        adapter = new DiscoveryResultsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final DiscoveryResultsAdapter.AdapterItem itemAtPosition = adapter.getItem(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private void onAdapterItemClick(DiscoveryResultsAdapter.AdapterItem item) {

        if (item.type == DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC) {
            final Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(ScanActivity.EXTRA_MAC_ADDRESS, macAddress);
            intent.putExtra(EXTRA_CHARACTERISTIC_UUID, item.uuid.toString());
            Log.i("lzk","characteristicUuid :"+item.uuid );
            setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
            finish();//此处一定要调用finish()方法

        } else {
            //noinspection ConstantConditions
            Snackbar.make(findViewById(android.R.id.content), "no action", Snackbar.LENGTH_SHORT).show();


        }
    }

    private void updateUI() {
        connectButton.setEnabled(!isConnected());
        connectButton.setText(isConnected() ? "disconnect" : "connect");

    }

    private boolean isConnected() {
        Log.i("lzk","RxBleConnection.RxBleConnectionState :"+bleDevice.getConnectionState().toString() );
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        updateUI();
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        failure.setText("reason:"+throwable);
        updateUI();
    }
}
