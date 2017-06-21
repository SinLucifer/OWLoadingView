package com.sin.owloadingview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sin.overwatchloading.OverWatchLoadingView;

public class MainActivity extends AppCompatActivity {

    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final OverWatchLoadingView owView = (OverWatchLoadingView)findViewById(R.id.loading);
        final Button bn = (Button)findViewById(R.id.bn);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    owView.start();
                    bn.setText("停止");
                    isStart = true;
                } else {
                    owView.stop();
                    bn.setText("启动");
                    isStart = false;
                }
            }
        });

    }
}
