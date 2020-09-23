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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gio.test.three.autotrack.R;

import java.util.ArrayList;

public class ExpandableListViewActivity extends Activity {
    private static final String TAG = "ExpandableListViewActivity";

    private ArrayList<String> mGroupList;
    private ArrayList<ArrayList<String>> mItemSet;

    private void initData() {
        mGroupList = new ArrayList<>();
        mGroupList.add("我的家人");
        mGroupList.add("我的朋友");
        mGroupList.add("黑名单");
        mItemSet = new ArrayList<>();
        ArrayList<String> itemList1 = new ArrayList<>();
        itemList1.add("大妹");
        itemList1.add("二妹");
        itemList1.add("三妹");
        ArrayList<String> itemList2 = new ArrayList<>();
        itemList2.add("大美");
        itemList2.add("二美");
        itemList2.add("三美");
        ArrayList<String> itemList3 = new ArrayList<>();
        itemList3.add("狗蛋");
        itemList3.add("二丫");
        mItemSet.add(itemList1);
        mItemSet.add(itemList2);
        mItemSet.add(itemList3);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable_list_view);
        initData();
        ExpandableListView expandableListView = findViewById(R.id.expand_lv);
        LayoutInflater lif = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View headerView1 = lif.inflate(R.layout.list_header, null);
        TextView textView1 = headerView1.findViewById(R.id.tv_title);
        textView1.setText("headerView1");
        headerView1.setOnClickListener(v -> Log.e(TAG, "headerView1 Click: "));
        expandableListView.addHeaderView(headerView1);

        View headerView2 = lif.inflate(R.layout.list_header, null);
        TextView textView2 = headerView2.findViewById(R.id.tv_title);
        textView2.setText("headerView2");
        headerView2.setOnClickListener(v -> Log.e(TAG, "headerView2 Click: "));
        expandableListView.addHeaderView(headerView2);

        View footerView1 = lif.inflate(R.layout.list_footer, null);
        TextView textView3 = footerView1.findViewById(R.id.tv_title);
        textView3.setText("footerView1");
        footerView1.setOnClickListener(v -> Log.e(TAG, "footerView1 Click: "));
        expandableListView.addFooterView(footerView1);

        View footerView2 = lif.inflate(R.layout.list_footer, null);
        TextView textView4 = footerView2.findViewById(R.id.tv_title);
        textView4.setText("footerView2");
        footerView2.setOnClickListener(v -> Log.e(TAG, "footerView2 Click: "));
        expandableListView.addFooterView(footerView2);

        MyAdapter adapter = new MyAdapter(this, mGroupList, mItemSet);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Log.e(TAG, "OnGroupClick: ");
                return false;
            }
        });
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Log.e(TAG, "OnChildClick: ");
            return true;
        });
    }

    private static class MyAdapter extends BaseExpandableListAdapter {
        private final Context mContext;
        private final ArrayList<String> mGroup;
        private final ArrayList<ArrayList<String>> mItemList;
        private final LayoutInflater mInflater;

        private MyAdapter(Context context, ArrayList<String> group, ArrayList<ArrayList<String>> itemList) {
            this.mContext = context;
            this.mGroup = group;
            this.mItemList = itemList;
            mInflater = LayoutInflater.from(context);
        }

        //父项的个数
        @Override
        public int getGroupCount() {
            return mGroup.size();
        }

        //某个父项的子项的个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return mItemList.get(groupPosition).size();
        }

        //获得某个父项
        @Override
        public Object getGroup(int groupPosition) {
            return mGroup.get(groupPosition);
        }

        //获得某个子项
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mItemList.get(groupPosition).get(childPosition);
        }

        //父项的Id
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        //子项的id
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        //获取父项的view
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_group, parent, false);
            }
            String group = mGroup.get(groupPosition);
            TextView tvGroup = (TextView) convertView.findViewById(R.id.tv_group);
            tvGroup.setText(group);
            return convertView;
        }

        //获取子项的view
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final String child = mItemList.get(groupPosition).get(childPosition);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_item, parent, false);
            }
            TextView tvChild = convertView.findViewById(R.id.tv_name);
            TextView tvRemark = convertView.findViewById(R.id.tv_remark);
            tvChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, child, Toast.LENGTH_SHORT).show();
                }
            });
            tvChild.setText(child);
            tvRemark.setText(child + " Remark");
            return convertView;
        }

        //子项是否可选中,如果要设置子项的点击事件,需要返回true
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}