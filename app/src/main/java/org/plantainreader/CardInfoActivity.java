package org.plantainreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class CardInfoActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, ReadingTagActivity.class);
        intent.setAction(getIntent().getAction());
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }
}
