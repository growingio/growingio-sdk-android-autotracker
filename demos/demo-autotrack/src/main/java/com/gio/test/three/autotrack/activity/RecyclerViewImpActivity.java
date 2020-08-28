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

package com.gio.test.three.autotrack.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gio.test.three.autotrack.R;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecyclerViewImpActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_imp);
        RecyclerView recyclerView = findViewById(R.id.rv_imp);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        findViewById(R.id.btn_imp_notify).setOnClickListener(v -> {
            adapter.notifyDataSetChanged();
        });
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_imp, parent, false);
            return new ViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.mLeft.setText("position: " + position);
            holder.mLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("position", position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (position == 0) {
                GrowingAutotracker.get().trackViewImpression(holder.itemView, "test_impression_0");
            }

            if (position == 99) {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                GrowingAutotracker.get().trackViewImpression(holder.itemView, "test_impression_99", map);
            }
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mLeft;

        ViewHolder(View itemView) {
            super(itemView);
            mLeft = itemView.findViewById(R.id.tv_left);
            TextView middle = itemView.findViewById(R.id.tv_middle);
            TextView right = itemView.findViewById(R.id.tv_right);
            middle.setText("中间部分");
            right.setText("两边部分");
        }
    }
}