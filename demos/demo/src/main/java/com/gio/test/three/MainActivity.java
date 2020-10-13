/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gio.test.three;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends ListActivity {
    private static final String TAG = "MainActivity";

    private List<ResolveInfo> mInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        Intent intent = getIntent();
        Log.e(TAG, "onCreate: intent.getData() = " + intent.getData());
        super.onCreate(savedInstanceState);
        new LoadTask().execute();
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ResolveInfo resolveInfo = mInfos.get(position);
        try {
            startActivity(new Intent(this, Class.forName(resolveInfo.activityInfo.name)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadTask extends AsyncTask<Void, Void, List<ResolveInfo>> {
        @Override
        protected List<ResolveInfo> doInBackground(Void... args) {
            PackageManager manager = getPackageManager();
            Intent intent = new Intent("com.gio.test.three.Entry");
            return manager.queryIntentActivities(intent, 0);
        }

        @Override
        protected void onPostExecute(List<ResolveInfo> resolveInfos) {
            mInfos = resolveInfos;
            setListAdapter(new Adapter());

        }
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mInfos == null ? 0 : mInfos.size();
        }

        @Override
        public ResolveInfo getItem(int position) {
            return mInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new ViewHolder(rootView);
                rootView.setTag(viewHolder);
                convertView = rootView;
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = getItem(position);
            String activityClassName = info.activityInfo.name;
            try {
                Class activityClass = Class.forName(activityClassName);
                ModuleEntry entry = (ModuleEntry) activityClass.getAnnotation(ModuleEntry.class);
                viewHolder.mTextView.setText(entry.value());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        private TextView mTextView;

        ViewHolder(View rootView) {
            mTextView = rootView.findViewById(android.R.id.text1);
        }
    }
}
