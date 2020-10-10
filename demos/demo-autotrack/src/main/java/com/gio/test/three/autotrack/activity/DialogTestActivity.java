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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.gio.test.three.autotrack.R;

public class DialogTestActivity extends Activity {
    private static final String TAG = "DialogTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_test);
        findViewById(R.id.btn_show_alert_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog();
            }
        });

        findViewById(R.id.btn_show_no_title_alert_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showNoTitleDialog();
            }
        });

        findViewById(R.id.btn_show_list_alert_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showListDialog();
            }
        });

        Button showPopupWindow = findViewById(R.id.btn_show_popup_window);
        showPopupWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.popup_window_preview, null);
                PopupWindow popupBigPhoto = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupBigPhoto.setOutsideTouchable(true);
                popupBigPhoto.showAsDropDown(showPopupWindow);
                view.findViewById(R.id.btn_preview).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                });
            }
        });

        findViewById(R.id.btn_show_popup_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(DialogTestActivity.this, v);
                // 获取布局文件
                popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });
            }
        });

        findViewById(R.id.btn_show_activity_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DialogTestActivity.this, AddPeopleDialogActivity.class));
            }
        });
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(DialogTestActivity.this)
                .setTitle("AlertDialog Title")
                .setMessage("测试AlertDialog")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    private void showNoTitleDialog() {
        new AlertDialog.Builder(DialogTestActivity.this)
                .setMessage("这是一个没有标题的AlertDialog")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void showListDialog() {
        new AlertDialog
                .Builder(this)
                .setItems(new String[]{"科目一", "科目二", "科目三"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DialogTestActivity.this, "选择了第" + which + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
    }
}