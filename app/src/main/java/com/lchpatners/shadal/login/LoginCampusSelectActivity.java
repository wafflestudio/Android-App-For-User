package com.lchpatners.shadal.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.lchpatners.shadal.R;
import com.lchpatners.shadal.RootActivity;
import com.lchpatners.shadal.util.LogUtils;

/**
 * Created by youngkim on 2015. 8. 20..
 */
public class LoginCampusSelectActivity extends ActionBarActivity {
    private static final String TAG = LogUtils.makeTag(LoginCampusSelectActivity.class);

    private Toolbar tbNavbar;
    private ListView lvCampusList;
    private Button buConfirmSelection;
    private LoginController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_campus_select);

        controller = new LoginController(LoginCampusSelectActivity.this);
        controller.drawCampusList();

        tbNavbar = (Toolbar) findViewById(R.id.LoginCampusSelect_toolBar);
        tbNavbar.setTitle("캠퍼스 선택하기");
        setSupportActionBar(tbNavbar);

        lvCampusList = (ListView) findViewById(R.id.LoginCampusSelect_campusList);
        LayoutInflater inflater = getLayoutInflater();
        FrameLayout listFooterView = (FrameLayout) inflater.inflate(
                R.layout.list_footer, null);
        lvCampusList.addFooterView(listFooterView);


        buConfirmSelection = (Button) findViewById(R.id.LoginCampusSelect_confirmButton);
        buConfirmSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controller.isCampusSelected()) {
                    controller.setCampus();
                    Intent intent = new Intent(LoginCampusSelectActivity.this, RootActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
}