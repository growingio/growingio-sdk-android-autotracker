package com.growingio.example;

import static android.content.pm.PackageManager.GET_SIGNATURES;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView sdkInfo = findViewById(R.id.main_tv_sdkInfo);
        sdkInfo.setText(getSdkInfo());

        TextView apkInfo = findViewById(R.id.main_tv_apkInfo);
        apkInfo.setText(getApkInfo());
        
        findViewById(R.id.main_btn_startInspect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, InspectActivity.class));
            }
        });
    }

    private String getSdkInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sdk基本信息").append("\n")
                .append("sdk名称: ").append("GrowingIO数据采集SDK").append("\n")
                .append("版本: ").append("V3.3.4").append("\n")
                .append("开发者: ").append("sdk-integration@growingio.com").append("\n")
                .append("MD5: ").append("DBCFCBD4627F22208B41ECD81CD6B980").append("\n")
                .append("编译时间: ").append("2022-03-02").append("\n")
                .append("送检功能: ").append("数据采集功能");
        return stringBuilder.toString();
    }

    private String getApkInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("样本App基本信息").append("\n")
                .append("样本名称: ").append("example").append("\n")
                .append("版本: ").append("1.0").append("\n")
                .append("送检方: ").append("北京易数科技有限公司").append("\n")
                .append("Apk签名MD5（非apk文件MD5）: ").append(getApkMd5()).append("\n")
                .append("打包时间: ").append("2022-03-02");
        return stringBuilder.toString();
    }

    private String getApkMd5() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null && packageInfo.signatures.length > 0) {
            byte[] bytes = packageInfo.signatures[0].toByteArray();
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            md5.update(bytes);
            return toHexString(md5.digest());
        }

        return "";
    }

    private String toHexString(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
            return null;
        }
        StringBuilder localStringBuilder = new StringBuilder(2 * paramArrayOfByte.length);
        for (int i = 0; ; i++) {
            if (i >= paramArrayOfByte.length) {
                return localStringBuilder.toString().toUpperCase();
            }
            String str = Integer.toString(0xFF & paramArrayOfByte[i], 16);
            if (str.length() == 1) {
                str = "0" + str;
            }
            localStringBuilder.append(str);
        }
    }
}