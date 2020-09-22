package com.example.drs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

public class DescriptionActivity extends AppCompatActivity {
    private TextView clickedAreaName;
    private TextView AreaDescription;
    private WebView GifWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        String CurrentAreaName=getIntent().getStringExtra("CLICKED_NAME");
        String CurrentAreaDescription=getIntent().getStringExtra("CLICKED_AREA_DESCRIPTION");
        clickedAreaName= (TextView)findViewById(R.id.areaNameid);
        AreaDescription=(TextView)findViewById(R.id.AreaDescription);
        clickedAreaName.setText(CurrentAreaName);
        AreaDescription.setText(CurrentAreaDescription);
    }
}