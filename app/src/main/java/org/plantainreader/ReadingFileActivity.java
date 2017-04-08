package org.plantainreader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadingFileActivity extends BaseActivity {
    public static final String LOG = "ReadingFileActivity";
    private Dump dump;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    public void onNewIntent(Intent intent) {
        final String[] tagIds = getSavedTags();
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_list, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_tag_title));
        builder.setView(view);
        final Dialog dialog = builder.create();
        ListView tagList = (ListView)view.findViewById(R.id.dialog_list_1);
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                tagIds);
        tagList.setAdapter(tagAdapter);
        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                String tagId = tagIds[position];
                chooseDump(tagId);
            }
        });
        dialog.show();
    }

    public void chooseDump(final String tagId){
        File dumpDir = getApplicationContext().getDir(getString(R.string.dump_dir), Context.MODE_PRIVATE);
        final File dir = new File(dumpDir, tagId);
        final ArrayList<String> dumps = new ArrayList<>();
        for(File f: dir.listFiles()) {
            if(f.isFile()){
                dumps.add(f.getName());
            }
        }
        if(dumps.isEmpty()){
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_list, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(getString(R.string.choose_dump_title));
        final Dialog dialog = builder.create();
        ListView dumpList = (ListView)view.findViewById(R.id.dialog_list_1);
        ArrayAdapter<String> dumpAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                dumps.toArray(new String[0]));
        dumpList.setAdapter(dumpAdapter);
        dumpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                String dumpName = dumps.get(position);
                File f = new File(dir, dumpName);
                dump = new Dump(tagId);
                readFile(f);
            }
        });
        dialog.show();
    }

    private void readFile(File file) {
        try{
            FileReader reader = new FileReader(file);
            char[] data = new char[(int)file.length()];
            reader.read(data);
            dump.deserialize(new String(data));
            Intent i = new Intent(this, DumpActivity.class);
            i.putExtra(getString(R.string.dump_key), dump);
            startActivity(i);
        } catch (IOException ex) {
            updateWidgetText(R.id.status_text, getString(R.string.status_file_read_error));
        }
    }
}
