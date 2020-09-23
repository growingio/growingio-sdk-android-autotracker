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

package com.gio.test.three.autotrack.fragments.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.gio.test.three.autotrack.fragments.FragmentLifecycleCallback;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleMonitor;

public abstract class BaseFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.CREATED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.VIEW_CREATED);
    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.STARTED);
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.RESUMED);
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.PAUSED);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.STOPPED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.DESTROYED_VIEW);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentLifecycleMonitor.get().signalLifecycleChange(this, FragmentLifecycleCallback.Stage.DESTROYED);
    }
}
