package org.plantainreader;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BaseActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private AlertDialog mEnableNfc;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] savedTags = getSavedTags();
        Button readFileButton = (Button)findViewById(R.id.read_file_button);
        if(savedTags.length == 0) {
            readFileButton.setVisibility(View.INVISIBLE);
        } else {
            readFileButton.setVisibility(View.VISIBLE);
        }
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
            if(mEnableNfc != null) {
                mEnableNfc.hide();
            }
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
            if(mEnableNfc == null) {
                enableNfcDialog();
            }
            mEnableNfc.show();
        }
    }

    private void enableNfcDialog() {
        mEnableNfc = new AlertDialog.Builder(this).setTitle(R.string.nfc_dialog_title)
                .setMessage(R.string.nfc_dialog_message)
                .setPositiveButton(R.string.nfc_dialog_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            @SuppressLint("InlinedApi")
                            public void onClick(DialogInterface dialog, int which) {
                                // Goto NFC Settings.
                                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                            }
                        })
                .setNegativeButton(R.string.nfc_dialog_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Exit the App.
                                finish();
                            }
                }).create();
    }

    protected void onPause() {
        super.onPause();
        if (this.mNfcAdapter != null) {
            this.mNfcAdapter.disableForegroundDispatch(this);
        }
    }
}
