package org.plantainreader;

import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class ReadingTagActivity extends BaseActivity {
    public static final  String LOG = "ReadingTagActivity";
    public static final byte[] A4_KEY;
    public static final byte[] A5_KEY;
    public Tag tag;

    static {
        A4_KEY = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
        A5_KEY = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateWidgetText(R.id.status_text, getString(R.string.status_reading));
        Thread runner = new Thread() {
            @Override
            public void run() {
                resolveIntent(getIntent());
            }
        };
        runner.start();
    }


    public void onNewIntent(Intent intent) {
        Thread runner = new Thread() {
            @Override
            public void run() {
                resolveIntent(getIntent());
            }
        };
        runner.start();
    }

    private void resolveIntent(Intent intent) {
        Dump dump = readNfcTag(intent);
        Intent i = new Intent(this, DumpActivity.class);
        i.putExtra(getString(R.string.dump_key), dump);
        startActivity(i);
    }

    private Dump readNfcTag(Intent intent) {
        MifareClassic tech = null;
        final Button tryAgainButton = (Button) findViewById(R.id.read_again_button);

        Tag tag = intent.getParcelableExtra("android.nfc.extra.TAG");
        byte[] tagId = intent.getByteArrayExtra("android.nfc.extra.ID");
        this.tag = tag;
        updateWidgetText(R.id.status_text, getString(R.string.status_nfc_tag_found) + ", UID: " +
                DumpUtil.bytesToHex(tagId));
        try {
            try {
                tech = MifareClassic.get(tag);
            } catch (Exception e) {
                tech = MifareClassic.get(MifareClassicHelper.patchTag(tag));
            }
            tech.connect();

            Dump dump = new Dump(DumpUtil.bytesToHex(tagId));
            updateWidgetText(R.id.status_text, getString(R.string.status_reading_sector_4));
            if (!tech.authenticateSectorWithKeyA(4, A4_KEY)) {
                updateWidgetText(R.id.status_text,
                        getString(R.string.status_reading_sector_4_failed));
                throw new IOException();
            }
            dump.readSector(tech, 4);
            updateWidgetText(R.id.status_text, getString(R.string.status_reading_sector_5));
            if (!tech.authenticateSectorWithKeyA(5, A5_KEY)) {
                updateWidgetText(R.id.status_text,
                        getString(R.string.status_reading_sector_5_failed));
                throw new IOException();
            }
            dump.readSector(tech, 5);
            return dump;
        } catch (IOException ignored) {
            updateWidgetText(R.id.status_text, getString(R.string.status_connection_lost));
        } catch (Throwable exception) {
            updateWidgetText(R.id.status_text, getString(R.string.status_unhandled_error) +
                    " " + exception.getMessage());
        } finally {
            tryAgainButton.post(new Runnable() {
                @Override
                public void run() {
                    tryAgainButton.setVisibility(View.VISIBLE);
                }
            });
            if (tech != null && tech.isConnected()) {
                try {
                    tech.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public void readAgainClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
