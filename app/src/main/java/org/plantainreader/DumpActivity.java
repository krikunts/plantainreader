package org.plantainreader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DumpActivity extends BaseActivity {
    public static final String LOG = "DumpActivity";

    public Dump dump;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    public void onNewIntent(Intent intent) {
        Bundle b = getIntent().getExtras();
        dump = b.getParcelable(getString(R.string.dump_key));
        updateWidgetText(R.id.balance_value, dump.getBalance());
        updateWidgetText(R.id.last_travel_cost,
                getString(R.string.last_travel_cost_label) + " " + dump.getLastTravelCost());
        final Calendar lastTravelDate = dump.getLastDate();
        final TextView travel_count_label = (TextView) findViewById(R.id.travel_count_label);
        travel_count_label.post(new Runnable() {
            @Override
            public void run() {
                travel_count_label.setVisibility(View.VISIBLE);
                travel_count_label.setText(getString(R.string.travel_count_label) + " " +
                        DumpUtil.formatDateMY(lastTravelDate) + ":");
            }
        });
        updateWidgetText(R.id.travel_count_value,
                getString(R.string.travel_subway) + " " + dump.getSubwayTravelCount() + ",\n" +
                        getString(R.string.travel_on_ground) + " " + dump.getOnGroundTravelCount());
        updateWidgetText(R.id.last_travel_date_value, getString(R.string.last_travel_date_label) +
                " " + DumpUtil.formatDateFull(lastTravelDate));
        Calendar lastPaymentDate = dump.getLastPaymentDate();
        updateWidgetText(R.id.last_payment,
                getString(R.string.last_payment_date_label) + " " +
                        DumpUtil.formatDateFull(lastPaymentDate) + " " +
                        getString(R.string.last_payment_value_label) + " " +
                        dump.getLastPaymentValue());
        updateWidgetText(R.id.status_text, getString(R.string.status_success));
        Log.i(LOG, "validator ID: " + dump.getLastValidator());
        final Button readAgainButton = (Button) findViewById(R.id.read_again_button);
        readAgainButton.post(new Runnable() {
            @Override
            public void run() {
                readAgainButton.setVisibility(View.VISIBLE);
            }
        });
        final Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.post(new Runnable() {
            @Override
            public void run() {
                saveButton.setVisibility(View.VISIBLE);
            }
        });

    }

    public void saveToFileClick(View v) {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
                Locale.getDefault());
        fmt.setCalendar(calendar);
        String dumpName = fmt.format(calendar.getTime());
        Context context = getApplicationContext();
        File dumpDir = context.getDir(getString(R.string.dump_dir), Context.MODE_PRIVATE);
        File dir = new File(dumpDir, dump.getTagName());
        if(!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, dumpName);
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(dump.serialize());
            writer.flush();
            writer.close();
            updateWidgetText(R.id.status_text, getString(R.string.status_file_write_ok));
            Button readFileButton = (Button)findViewById(R.id.read_file_button);
            readFileButton.setVisibility(View.VISIBLE);
        } catch (IOException ex) {
            updateWidgetText(R.id.status_text, getString(R.string.status_file_write_error));
        }
    }
}
