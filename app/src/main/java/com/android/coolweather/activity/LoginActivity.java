package com.android.coolweather.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.coolweather.R;
import com.android.coolweather.util.ActivityCollector;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cherish on 2016/7/29.
 */
public class LoginActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_layout);
        ActivityCollector.addActivity(this);
        TextView welcomeText = (TextView)findViewById(R.id.welcome);
        ImageView shouyeIcon = (ImageView)findViewById(R.id.shouye_icon);
        TextView shouyeText = (TextView)findViewById(R.id.shouye_text);
        welcomeText.setText(getResources().getString(R.string.welcome));
        shouyeIcon.setBackgroundResource(R.mipmap.icon_weather);
        shouyeText.setText(getResources().getString(R.string.app_name));

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if(ActivityCollector.activities != null){
                    Intent intent = new Intent(LoginActivity.this,ChooseAreaActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        };
        timer.schedule(timerTask, 2 * 1000);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        ActivityCollector.finishAll();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
