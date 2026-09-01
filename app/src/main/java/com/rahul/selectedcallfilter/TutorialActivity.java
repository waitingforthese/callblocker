package com.rahul.selectedcallfilter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

public class TutorialActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        View root = findViewById(R.id.tutorialRoot);
        if (root != null) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(v.getPaddingLeft(), insets.getSystemWindowInsetTop(),
                        v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                return insets;
            });
            root.requestApplyInsets();
        }

        findViewById(R.id.closeTutorial).setOnClickListener(v -> finish());
    }
}
