package org.plantainreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        initAdapter();
    }

    protected void onResume() {
        super.onResume();
        initAdapter();
        if (this.isAdapterEnabled()) {
            this.mNfcAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, (String[][]) null);
        }
    }

    public boolean isAdapterEnabled() {
        return this.mNfcAdapter != null && this.mNfcAdapter.isEnabled();
    }

    public void initAdapter() {
        TextView statusText = (TextView) findViewById(R.id.status_text);
        if (this.isAdapterEnabled()) {
            statusText.setText(getString(R.string.status_wait));
            if (!getPackageManager().hasSystemFeature("com.nxp.mifare")) {
                statusText.append("\n\n" + getString(R.string.compat_warning));
            }
            if (this.mPendingIntent == null) {
                Intent intent = new Intent(this, ReadingTagActivity.class);
                intent.addFlags(1879048192);
                this.mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            }
        } else {
            statusText.setText(getString(R.string.nfc_disabled));
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mNfcAdapter != null) {
            this.mNfcAdapter.disableForegroundDispatch(this);
        }
    }
}
