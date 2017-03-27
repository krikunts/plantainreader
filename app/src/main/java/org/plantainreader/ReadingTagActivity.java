package org.plantainreader;

import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class ReadingTagActivity extends AppCompatActivity {
    public static final  String LOG = "ReadingTagActivity";
    public static final byte[] A4_KEY;
    public static final byte[] A5_KEY;
    public Tag tag;
    public Dump dump;

    static {
        A4_KEY = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
        A5_KEY = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
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
        MifareClassic tech = null;
        final Button tryAgainButton = (Button) findViewById(R.id.read_again_button);

        Tag tag = intent.getParcelableExtra("android.nfc.extra.TAG");
        byte[] tagId = intent.getByteArrayExtra("android.nfc.extra.ID");

        this.tag = tag;
        updateWidgetText(R.id.status_text, getString(R.string.status_nfc_tag_found) + ", UID: " +
                         DumpUtil.getHexString(tagId, "-"));
        try {
            try {
                tech = MifareClassic.get(tag);
            } catch (Exception e) {
                tech = MifareClassic.get(MifareClassicHelper.patchTag(tag));
            }
            tech.connect();

            dump = new Dump(tagId);
            updateWidgetText(R.id.status_text, getString(R.string.status_reading_sector_4));
            if (!tech.authenticateSectorWithKeyA(4, A4_KEY)) {
                updateWidgetText(R.id.status_text,
                                 getString(R.string.status_reading_sector_4_failed));
                throw new IOException();
            }
            readSector(tech, 4, dump.sector4);
            updateWidgetText(R.id.status_text, getString(R.string.status_reading_sector_5));
            if (!tech.authenticateSectorWithKeyA(5, A5_KEY)) {
                updateWidgetText(R.id.status_text,
                                 getString(R.string.status_reading_sector_5_failed));
                throw new IOException();
            }
            readSector(tech, 5, dump.sector5);
            updateWidgetText(R.id.balance_value, dump.getBalance());
            updateWidgetText(R.id.last_travel_cost,
                             getString(R.string.last_travel_cost_label) + " " + dump.getLastTravelCost());
            final TextView travel_count_label = (TextView) findViewById(R.id.travel_count_label);
            travel_count_label.post(new Runnable() {
                @Override
                public void run() {
                    travel_count_label.setVisibility(View.VISIBLE);
                }
            });
            updateWidgetText(R.id.travel_count_value, 
                             getString(R.string.travel_subway) + " " + dump.getSubwayTravelCount() + ",\n" +
                             getString(R.string.travel_on_ground) + " " + dump.getOnGroundTravelCount());
            updateWidgetText(R.id.last_travel_date_value, getString(R.string.last_travel_date_label) +
                             " " + dump.getLastDate());
            updateWidgetText(R.id.status_text, getString(R.string.status_success));
            Log.i(LOG, "validator ID: " + dump.getLastValidator());

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
    }

    private void readSector(MifareClassic tech, int sectorId, byte[][] result) throws IOException{
        for (int i = 0, block = tech.sectorToBlock(sectorId); i < Dump.SECTOR_SIZE; i++) {
            result[i] = tech.readBlock(block + i);
        }
    }

    public void readAgainClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateWidgetText(final int widgetId, final String text) {
        Log.i(LOG, text + "\n");
        Runnable r =  new Runnable() {
            @Override
            public void run() {
                TextView widget = (TextView) findViewById(widgetId);
                widget.setText(text);
            };
        };
        runOnUiThread(r);
    }
}
