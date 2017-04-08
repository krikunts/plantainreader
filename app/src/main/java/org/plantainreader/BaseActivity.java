package org.plantainreader;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {
    public static final String LOG = "BaseActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    protected void updateWidgetText(final int widgetId, final String text) {
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

    public void readFromFileClick(View v) {
        Intent intent = new Intent(this, ReadingFileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

    public void readAgainClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

    public String[] getSavedTags() {
        File dumpDir = getApplicationContext().getDir(getString(R.string.dump_dir), Context.MODE_PRIVATE);
        final ArrayList<String> tagIds = new ArrayList<>();
        for(File f: dumpDir.listFiles()) {
            if(f.isDirectory()){
                tagIds.add(f.getName());
            }
        }
        return tagIds.toArray(new String[0]);
    }
}
