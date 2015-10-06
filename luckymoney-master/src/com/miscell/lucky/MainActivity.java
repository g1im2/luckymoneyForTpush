package com.miscell.lucky;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;



public class MainActivity extends Activity {
    private static final Intent sSettingsIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private TextView mAccessibleLabel;
    private TextView mNotificationLabel;
    private TextView mLabelText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
     // 开启logcat输出，方便debug，发布时请关闭
     XGPushConfig.enableDebug(this, true);
     // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
     // 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
     // 具体可参考详细的开发指南
     // 传递的参数为ApplicationContext
     Context context = getApplicationContext();
     XGPushManager.registerPush(context);	

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final float density = metrics.density;
        final int screenWidth = metrics.widthPixels;

        int width = (int) (screenWidth - (density * 12 + .5f) * 2);
        int height = (int) (366.f * width / 1080);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        ImageView imageView1 = (ImageView) findViewById(R.id.image_accessibility);
        ImageView imageView2 = (ImageView) findViewById(R.id.image_notification);

        mAccessibleLabel = (TextView) findViewById(R.id.label_accessible);
        mNotificationLabel = (TextView) findViewById(R.id.label_notification);
        mLabelText = (TextView) findViewById(R.id.label_text);

        imageView1.setLayoutParams(lp);
        imageView2.setLayoutParams(lp);

        if (Build.VERSION.SDK_INT >= 18) {
            imageView2.setVisibility(View.VISIBLE);
            mNotificationLabel.setVisibility(View.VISIBLE);
            findViewById(R.id.button_notification).setVisibility(View.VISIBLE);
        } else {
            imageView2.setVisibility(View.GONE);
            mNotificationLabel.setVisibility(View.GONE);
            findViewById(R.id.button_notification).setVisibility(View.GONE);
        }

//        imageView1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("test", "# fired");
//                unlockScreen();
//
//            }
//        }, 5000L);
    }


    @Override
    protected void onResume() {
        super.onResume();
        changeLabelStatus();
    }

    private void changeLabelStatus() {
        boolean isAccessibilityEnabled = isAccessibleEnabled();
        mAccessibleLabel.setTextColor(isAccessibilityEnabled ? 0xFF009588 : Color.RED);
        mAccessibleLabel.setText(isAccessibleEnabled() ? "杈呭姪鍔熻兘宸叉墦寮�" : "杈呭姪鍔熻兘鏈墦寮�");
        mLabelText.setText(isAccessibilityEnabled ? "濂戒簡~浣犲彲浠ュ幓鍋氬叾浠栦簨鎯呬簡锛屾垜浼氳嚜鍔ㄧ粰浣犳姠绾㈠寘鐨�" : "璇锋墦寮�寮�鍏冲紑濮嬫姠绾㈠寘");

        if (Build.VERSION.SDK_INT >= 18) {
            boolean isNotificationEnabled = isNotificationEnabled();
            mNotificationLabel.setTextColor(isNotificationEnabled ? 0xFF009588 : Color.RED);
            mNotificationLabel.setText(isNotificationEnabled ? "鎺ユ敹閫氱煡宸叉墦寮�" : "鎺ユ敹閫氱煡鏈墦寮�");

            if (isAccessibilityEnabled && isNotificationEnabled) {
                mLabelText.setText("濂戒簡~浣犲彲浠ュ幓鍋氬叾浠栦簨鎯呬簡锛屾垜浼氳嚜鍔ㄧ粰浣犳姠绾㈠寘鐨�");
            } else {
                mLabelText.setText("璇锋妸涓や釜寮�鍏抽兘鎵撳紑寮�濮嬫姠绾㈠寘");
            }
        }
    }

    public void onNotificationEnableButtonClicked(View view) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void onSettingsClicked(View view) {
        startActivity(sSettingsIntent);
    }

    private boolean isAccessibleEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo info : runningServices) {
            if (info.getId().equals(getPackageName() + "/.MonitorService")) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotificationEnabled() {
        ContentResolver contentResolver = getContentResolver();
        String enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

        if (!TextUtils.isEmpty(enabledListeners)) {
            return enabledListeners.contains(getPackageName() + "/" + getPackageName() + ".NotificationService");
        } else {
            return false;
        }
    }

    private void showEnableAccessibilityDialog() {
        final ConfirmDialog dialog = new ConfirmDialog(this);
        dialog.setTitle("閲嶈!").setMessage("您需要打开\"有红包\"的辅助功能选项才能抢微信红包")
                .setPositiveButton("鎵撳紑", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(sSettingsIntent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("鍙栨秷", null);
        dialog.show();
    }
}
