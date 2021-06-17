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
package com.growingio.android.sdk.track.providers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class RobolectricActivity extends Activity {

    public ActivityLifecycleEvent.EVENT_TYPE state;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        state = ON_CREATED;
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("this is cpacm");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //can circle
            }
        });

        ImageView imageView = new ImageView(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(imageView);
        setContentView(linearLayout);
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
}
