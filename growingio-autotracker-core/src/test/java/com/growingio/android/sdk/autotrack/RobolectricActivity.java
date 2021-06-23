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
package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;

import static android.widget.LinearLayout.VERTICAL;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_DESTROYED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_NEW_INTENT;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_SAVE_INSTANCE_STATE;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED;

public class RobolectricActivity extends FragmentActivity {

    private ActivityLifecycleEvent.EVENT_TYPE state;

    private TextView textView;
    private RecyclerView recyclerView;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        state = ON_CREATED;
        super.onCreate(savedInstanceState);
        textView = new TextView(this);
        textView.setText("this is cpacm");

        recyclerView = new RecyclerView(this);
        recyclerView.setAdapter(new TestAdapter(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageView = new ImageView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(imageView);
        linearLayout.addView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(linearLayout);
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        wlp.packageName = "com.cpacm.test";
        getWindow().getDecorView().setLayoutParams(wlp);
    }

    public android.app.Fragment attachFragment(android.app.Fragment appFragment) {
        getFragmentManager().beginTransaction()
                .add(appFragment, "app")
                .commit();
        return appFragment;
    }

    public void attachFragmentX(Fragment supportFragment) {
        getSupportFragmentManager().beginTransaction()
                .add(supportFragment, "androidX")
                .commit();
    }

    public ActivityLifecycleEvent.EVENT_TYPE getState() {
        return state;
    }

    public RecyclerView getRecyclerView() {
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 1000);
        return recyclerView;
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    @Override
    protected void onResume() {
        state = ON_RESUMED;
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        state = ON_NEW_INTENT;
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        state = ON_STARTED;
        super.onStart();
    }

    @Override
    protected void onPause() {
        state = ON_PAUSED;
        super.onPause();
    }

    @Override
    protected void onStop() {
        state = ON_STOPPED;
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        state = ON_SAVE_INSTANCE_STATE;
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        state = ON_DESTROYED;
        super.onDestroy();
    }


    public static class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

        private final Context context;

        public TestAdapter(Context context) {
            this.context = context;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            return new TestViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull RobolectricActivity.TestAdapter.TestViewHolder holder, int position) {
            if (holder.itemView instanceof TextView) {
                ((TextView) holder.itemView).setText("position:" + position);
            }
        }

        @Override
        public int getItemCount() {
            return 10;
        }

        public static class TestViewHolder extends RecyclerView.ViewHolder {

            public TestViewHolder(View view) {
                super(view);
            }

        }
    }


}
