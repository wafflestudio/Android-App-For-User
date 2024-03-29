package com.lchpatners.shadal.login;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.lchpatners.shadal.R;
import com.lchpatners.shadal.setting.CampusChangeController;
import com.lchpatners.shadal.util.AnalyticsHelper;
import com.lchpatners.shadal.util.LogUtils;

/**
 * Created by youngkim on 2015. 8. 20..
 */
public class LoginCampusSelectActivity extends ActionBarActivity {
    private static final String TAG = LogUtils.makeTag(LoginCampusSelectActivity.class);

    private Toolbar tbNavbar;
    private ListView lvCampusList;
    private Button buConfirmSelection;
    private CampusChangeController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_campus_select);

        controller = new CampusChangeController(LoginCampusSelectActivity.this);
        controller.drawCampusList();

        tbNavbar = (Toolbar) findViewById(R.id.LoginCampusSelect_toolBar);
        tbNavbar.setTitle("캠퍼스 선택하기");
        setSupportActionBar(tbNavbar);

        lvCampusList = (ListView) findViewById(R.id.campusList);
        LayoutInflater inflater = getLayoutInflater();
        FrameLayout listFooterView = (FrameLayout) inflater.inflate(
                R.layout.list_footer, null);
        lvCampusList.addFooterView(listFooterView);

        buConfirmSelection = (Button) findViewById(R.id.LoginCampusSelect_confirmButton);
        buConfirmSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controller.isCampusSelected()) {
                    Toast.makeText(LoginCampusSelectActivity.this, "음식점 정보를 받아옵니다", Toast.LENGTH_LONG).show();
                    controller.setCampus();

                    AnalyticsHelper analyticsHelper = new AnalyticsHelper(LoginCampusSelectActivity.this);
                    analyticsHelper.sendEvent("UX", "select_campus_start", controller.getCampus().getName_kor_short());
                    //Go to RootActivity at setCampus function
//                    Intent intent = new Intent(LoginCampusSelectActivity.this, RootActivity.class);
//                    startActivity(intent);
                }
            }
        });

        AnalyticsHelper analyticsHelper = new AnalyticsHelper(LoginCampusSelectActivity.this);
        analyticsHelper.sendScreen("캠퍼스 선택하기 화면");
    }
}
