package com.lchpatners.shadal.restaurant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.lchpatners.shadal.R;
import com.lchpatners.shadal.call.CallLog.CallLogController;
import com.lchpatners.shadal.call.RecentCallController;
import com.lchpatners.shadal.campus.CampusController;
import com.lchpatners.shadal.dao.Flyer;
import com.lchpatners.shadal.dao.Restaurant;
import com.lchpatners.shadal.restaurant.flyer.FlyerActivity;
import com.lchpatners.shadal.util.AnalyticsHelper;
import com.lchpatners.shadal.util.LogUtils;
import com.lchpatners.shadal.util.Preferences;
import com.lchpatners.shadal.util.RetrofitConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by YoungKim on 2015. 8. 26..
 */
public class RestaurantInfoController {
    public static final String FLYER_URLS = "flyer_urls";
    public static final String RESTAURANT_ID = "restaurant_id";
    public static final String RESTAURANT_PHONE_NUMBER = "restaurant_phone_number";
    public static final int GOOD = 1;
    public static final int BAD = 0;
    private static final String TAG = LogUtils.makeTag(RestaurantInfoActivity.class);
    private static final String BASE_URL = "http://www.shadal.kr:3000";
    private Activity mActivity;
    private Restaurant mRestaurant;
    private View mHeader;
    private RestaurantEvaluationController mRestaurantEvaluationController;

    private boolean likeBtnChecked = false;
    private boolean hateBtnChecked = false;
    private int goodCount = 0;
    private int badCount = 0;
    private int totalCount = 0;
    // if there's no pre-evaluate -> -1, good -> 1, bad -> 0
    private int userCount = -1;

    public RestaurantInfoController(Activity activity) {
        this.mActivity = activity;
        this.mRestaurantEvaluationController = new RestaurantEvaluationController(mActivity);
        // TODO: set Restaurant Info at constructor
    }

    public Restaurant getRestaurant(int restaurant_id) {
        Restaurant restaurant = null;

        Realm realm = Realm.getInstance(mActivity);
        try {
            realm.beginTransaction();
            RealmQuery<Restaurant> query = realm.where(Restaurant.class);
            restaurant = query.equalTo("id", restaurant_id).findFirst();
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }

        mRestaurant = restaurant;
        updateCurrentRestaurant();

        return restaurant;
    }

    public void setKAKAORestaurant(Restaurant restaurant) {
        mRestaurant = restaurant;
    }

    private void updateCurrentRestaurant() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new RetrofitConverter().createRestaurantConverter()))
                .setEndpoint(BASE_URL) // The base API endpoint.
                .build();

        RestaurantAPI restaurantAPI = restAdapter.create(RestaurantAPI.class);

        restaurantAPI.getRestaurant(mRestaurant.getId(), new Callback<Restaurant>() {
            @Override
            public void success(Restaurant restaurant, Response response) {
                Realm realm = Realm.getInstance(mActivity);
                realm.beginTransaction();
                try {
                    realm.copyToRealmOrUpdate(restaurant);
                    realm.commitTransaction();
                } catch (Exception e) {
                    realm.cancelTransaction();
                    e.printStackTrace();
                } finally {
                    realm.close();
                }

                setHeaderData();
                mRestaurant = restaurant;
                goodCount = restaurant.getTotal_number_of_goods();
                badCount = restaurant.getTotal_number_of_bads();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, error.toString());
                error.printStackTrace();
            }
        });
    }

    public void setHeader() {
        setHeaderWidget();
        setHeaderData();
        setHeaderButtonListener();
        setEvaluateButtonListener();
        attachHeader();
    }

    public void setFooter() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footer = inflater.inflate(R.layout.list_footer, null, false);
        ListView listView = (ListView) mActivity.findViewById(R.id.menu_list);

        if (listView.getFooterViewsCount() == 0) {
            listView.addFooterView(footer, null, false);
        }
    }

    private void setHeaderWidget() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeader = inflater.inflate(R.layout.menu_header, null, false);
    }

    private void setHeaderData() {
        checkPreEvaluateByUser();
        //TODO : update Evaluation

        setEvaluationBar();

        TextView retention = (TextView) mHeader.findViewById(R.id.retention);
        TextView numberOfMyCalls = (TextView) mHeader.findViewById(R.id.number_of_my_calls);
        TextView totalNumberOfCalls = (TextView) mHeader.findViewById(R.id.total_number_of_calls);
        TextView notice = (TextView) mHeader.findViewById(R.id.notice);
        LinearLayout noticeLayout = (LinearLayout) mHeader.findViewById(R.id.notice_layout);
        if (mRestaurant.getNotice() != null && mRestaurant.getNotice().length() > 0) {
            notice.setText(mRestaurant.getNotice());
        } else {
            noticeLayout.setVisibility(View.GONE);
        }

        retention.setText(Math.round(mRestaurant.getRetention() * 100) + "");
        numberOfMyCalls.setText(RecentCallController.getRecentCallCountByRestaurantId(mActivity, mRestaurant.getId()) + "");
        totalNumberOfCalls.setText(numberOfCallsFormatString(mRestaurant.getTotal_number_of_calls()));
    }

    private void setEvaluationBar() {
        float percent = 0;

        TextView tv_like = (TextView) mHeader.findViewById(R.id.tv_like);
        TextView tv_hate = (TextView) mHeader.findViewById(R.id.tv_hate);
        TextView tv_percent = (TextView) mHeader.findViewById(R.id.tv_percent);
        LinearLayout base_rating_bar = (LinearLayout) mHeader.findViewById(R.id.base_rating_bar);
        LinearLayout rating_bar = (LinearLayout) mHeader.findViewById(R.id.rating_bar);
        LinearLayout max_rating_bar = (LinearLayout) mHeader.findViewById(R.id.max_rating_bar);
        RelativeLayout rl_percent = (RelativeLayout) mHeader.findViewById(R.id.rl_percent);

        base_rating_bar.setVisibility(View.VISIBLE);
        max_rating_bar.setVisibility(View.INVISIBLE);
        rating_bar.setVisibility(View.VISIBLE);

        if (goodCount == 0 && badCount == 0) {
            percent = 50;
            rating_bar.getLayoutParams().width = (int) (base_rating_bar.getLayoutParams().width * (percent / 100));

            if (((ViewGroup) rl_percent).getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) ((ViewGroup) rl_percent).getLayoutParams()).leftMargin
                        = (rating_bar.getLayoutParams().width);
            }
        } else if (goodCount == 0) {
            if (((ViewGroup) rl_percent).getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) ((ViewGroup) rl_percent).getLayoutParams()).leftMargin
                        = 0;
            }

            rating_bar.setVisibility(View.INVISIBLE);
        } else if (badCount == 0) {
            rating_bar.setVisibility(View.INVISIBLE);
            max_rating_bar.setVisibility(View.VISIBLE);
            percent = 100;
            rating_bar.getLayoutParams().width = (int) (base_rating_bar.getLayoutParams().width);

            if (((ViewGroup) rl_percent).getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) ((ViewGroup) rl_percent).getLayoutParams()).leftMargin
                        = (rating_bar.getLayoutParams().width);
            }
        } else {
            percent = ((float) goodCount / ((float) goodCount + (float) badCount) * 100);
            rating_bar.getLayoutParams().width = (int) (base_rating_bar.getLayoutParams().width * (percent / 100));

            if (((ViewGroup) rl_percent).getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) ((ViewGroup) rl_percent).getLayoutParams()).leftMargin
                        = (rating_bar.getLayoutParams().width);
            }
        }

        tv_like.setText(goodCount + "");
        tv_hate.setText(badCount + "");
        tv_percent.setText(Math.round(percent) + "%");
    }

    private String numberOfCallsFormatString(int numberOfCalls) {
        if ((numberOfCalls >= 10) && (numberOfCalls < 100)) {
            numberOfCalls = (numberOfCalls / 10) * 10;
        } else if ((numberOfCalls >= 100)) {
            numberOfCalls = (numberOfCalls / 100) * 100;
        }

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String strNumberOfCalls = formatter.format(numberOfCalls);
        return strNumberOfCalls;
    }

    public void attachHeader() {
        ListView listView = (ListView) mActivity.findViewById(R.id.menu_list);
        if (listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(mHeader, null, false);
        }
    }

    public void setCallButtonListener() {
        Toolbar phoneButton = (Toolbar) mActivity.findViewById(R.id.bottom_bar);
        TextView phoneNumber = (TextView) mActivity.findViewById(R.id.phone_number);
        phoneNumber.setText(mRestaurant.getPhone_number());

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    AnalyticsHelper helper = new AnalyticsHelper(getApplication());
//                    helper.sendEvent("UX", "phonenumber_clicked", restaurant.getName());
                String number = "tel:" + mRestaurant.getPhone_number();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                mActivity.startActivity(intent);
                updateCallLog();

                AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                analyticsHelper.sendEvent("UX", "phonenumber_clicked", mRestaurant.getName());
                analyticsHelper.sendEvent("UX", "phonenumber_in_restaurant_clicked", mRestaurant.getName());

//                RootActivity.updateNavigationView(mActivity);
//                RecommendedRestaurantActivity.updateNavigationView(mActivity);
            }
        });
    }

    private void updateCallLog() {
        SystemClock.sleep(2 * 1000);
        RecentCallController.stackRecentCall(mActivity, mRestaurant.getId());
        CallLogController.sendCallLog(mActivity, mRestaurant.getId());
        setHeaderData();
    }

    public void setFlyerButtonListener() {
        ImageButton flyer = (ImageButton) mActivity.findViewById(R.id.flyer);
        RelativeLayout divider = (RelativeLayout) mActivity.findViewById(R.id.divider_layout);
        divider.setVisibility((mRestaurant.isHas_flyer()) ? View.VISIBLE : View.GONE);
        flyer.setVisibility((mRestaurant.isHas_flyer()) ? View.VISIBLE : View.GONE);

        flyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> flyer_urls = new ArrayList<String>();
                List<Flyer> flyers = mRestaurant.getFlyers();

                for (Flyer flyer : flyers) {
                    flyer_urls.add(flyer.getUrl());
                }

                Intent intent = new Intent(mActivity, FlyerActivity.class);
                intent.putExtra(FLYER_URLS, flyer_urls);
                intent.putExtra(RESTAURANT_ID, mRestaurant.getId());
                intent.putExtra(RESTAURANT_PHONE_NUMBER, mRestaurant.getPhone_number());
                mActivity.startActivity(intent);

                AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                analyticsHelper.sendEvent("UX", "flyer_in_restaurant_clicked", mRestaurant.getName());
            }
        });
    }

    private void checkPreEvaluateByUser() {
        int score = mRestaurantEvaluationController.getEvaluationByCurrentUser(mRestaurant.getId());

        final TextView click_evaluation = (TextView) mHeader.findViewById(R.id.click_evaluation);
        final ImageView ivEvaluation = (ImageView) mHeader.findViewById(R.id.iv_evaluation);
        final TextView tvEvaluation = (TextView) mHeader.findViewById(R.id.tv_evaluation);
        final LinearLayout btn_divider = (LinearLayout) mHeader.findViewById(R.id.btn_divider);

        final ImageButton btnHate = (ImageButton) mHeader.findViewById(R.id.btn_hate);
        final ImageButton btnLike = (ImageButton) mHeader.findViewById(R.id.btn_like);

        if (score != -1) {
            if (score == GOOD) {
                userCount = GOOD;
                likeBtnChecked = true;
                btnLike.setImageResource(R.drawable.icon_detail_page_btn_check);
                btnHate.setImageResource(R.drawable.icon_detail_page_btn_hate);
            } else if (score == BAD) {
                userCount = BAD;
                hateBtnChecked = true;
                btnHate.setImageResource(R.drawable.icon_detail_page_btn_check_selected);
                btnLike.setImageResource(R.drawable.icon_detail_page_btn_like);
            }
            click_evaluation.setVisibility(View.GONE);
            tvEvaluation.setVisibility(View.GONE);
            ivEvaluation.setVisibility(View.GONE);
            btn_divider.setVisibility(View.GONE);

            btnHate.setVisibility(View.VISIBLE);
            btnLike.setVisibility(View.VISIBLE);
        }

        goodCount = mRestaurant.getTotal_number_of_goods();
        badCount = mRestaurant.getTotal_number_of_bads();
    }

    private void setHeaderButtonListener() {
        final TextView click_share = (TextView) mHeader.findViewById(R.id.click_share);
        final TextView click_evaluation = (TextView) mHeader.findViewById(R.id.click_evaluation);

        final ImageView ivEvaluation = (ImageView) mHeader.findViewById(R.id.iv_evaluation);
        final TextView tvEvaluation = (TextView) mHeader.findViewById(R.id.tv_evaluation);

        final LinearLayout btn_divider = (LinearLayout) mHeader.findViewById(R.id.btn_divider);

        final ImageButton btnHate = (ImageButton) mHeader.findViewById(R.id.btn_hate);
        final ImageButton btnLike = (ImageButton) mHeader.findViewById(R.id.btn_like);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.click_share) {
                    try {
                        final KakaoLink kakaoLink = KakaoLink.getKakaoLink(mActivity);
                        final KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder
                                = kakaoLink.createKakaoTalkLinkMessageBuilder();
                        String campus_name = CampusController.getCurrentCampus(mActivity).getName_kor_short();
                        String text = mRestaurant.getName() + "(" + campus_name + ")" + "\n" + mRestaurant.getPhone_number();

                        final String linkContent =
                                kakaoTalkLinkMessageBuilder
                                        .addText(text)
                                        .addAppButton("캠퍼스:달 바로가기",
                                                new AppActionBuilder()
                                                        .addActionInfo(AppActionInfoBuilder
                                                                .createAndroidActionInfoBuilder()
                                                                .setExecuteParam("restaurant_id=" + mRestaurant.getId() + "&" + "campusName=" + campus_name)
                                                                .setMarketParam("referrer=kakaotalklink")
                                                                .build())
                                                        .addActionInfo(AppActionInfoBuilder
                                                                .createiOSActionInfoBuilder()
                                                                .setExecuteParam("restaurant_id=" + mRestaurant.getId() + "&" + "campusName=" + campus_name)
                                                                .build())
                                                        .build())
                                        .build();
                        kakaoLink.sendMessage(linkContent, mActivity);
                    } catch (KakaoParameterException e) {
                        e.printStackTrace();
                    } finally {
                        AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                        analyticsHelper.sendEvent("UX", "share_kakao_clicked", mRestaurant.getName());
                    }

                } else if (view.getId() == R.id.click_evaluation) {
                    click_evaluation.setVisibility(View.GONE);
                    tvEvaluation.setVisibility(View.GONE);
                    ivEvaluation.setVisibility(View.GONE);
                    btn_divider.setVisibility(View.GONE);

                    btnHate.setVisibility(View.VISIBLE);
                    btnLike.setVisibility(View.VISIBLE);
                }
            }
        };

        click_evaluation.setOnClickListener(listener);
        click_share.setOnClickListener(listener);

        if (likeBtnChecked || hateBtnChecked) {
            click_evaluation.setVisibility(View.GONE);
        }
    }

    private void setEvaluateButtonListener() {
        final ImageButton btnHate = (ImageButton) mHeader.findViewById(R.id.btn_hate);
        final ImageButton btnLike = (ImageButton) mHeader.findViewById(R.id.btn_like);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_like) {
                    if (!likeBtnChecked) {
                        mRestaurantEvaluationController.evaluate(GOOD, mRestaurant.getId());
                        UserPreferenceController.sendUserPreference(mRestaurant.getId(), GOOD, Preferences.getDeviceUuid(mActivity));
                        if (!likeBtnChecked) {
                            likeBtnChecked = !likeBtnChecked;
                            hateBtnChecked = !likeBtnChecked;
                            btnLike.setImageResource(R.drawable.icon_detail_page_btn_check);
                        } else {
                            likeBtnChecked = !likeBtnChecked;
                            hateBtnChecked = !likeBtnChecked;
                            btnLike.setImageResource(R.drawable.icon_detail_page_btn_like);
                        }
                        btnHate.setImageResource(R.drawable.icon_detail_page_btn_hate);
                        if (userCount == -1) {
                            goodCount += 1;
                            userCount = GOOD;
                        } else {
                            goodCount += 1;
                            badCount -= 1;
                        }
                        setEvaluationBar();
                    }
                    AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                    analyticsHelper.sendEvent("UX", "like_button_clicked", mRestaurant.getName());
                } else if (view.getId() == R.id.btn_hate) {
                    if (!hateBtnChecked) {
                        mRestaurantEvaluationController.evaluate(BAD, mRestaurant.getId());
                        UserPreferenceController.sendUserPreference(mRestaurant.getId(), BAD, Preferences.getDeviceUuid(mActivity));
                        if (!hateBtnChecked) {
                            hateBtnChecked = !hateBtnChecked;
                            likeBtnChecked = !hateBtnChecked;
                            btnHate.setImageResource(R.drawable.icon_detail_page_btn_check_selected);
                        } else {
                            hateBtnChecked = !hateBtnChecked;
                            likeBtnChecked = !hateBtnChecked;
                            btnHate.setImageResource(R.drawable.icon_detail_page_btn_hate);
                        }
                        btnLike.setImageResource(R.drawable.icon_detail_page_btn_like);
                        if (userCount == -1) {
                            badCount += 1;
                            userCount = BAD;
                        } else {
                            goodCount -= 1;
                            badCount += 1;
                        }
                        setEvaluationBar();
                    }
                    AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                    analyticsHelper.sendEvent("UX", "dislike_button_clicked", mRestaurant.getName());
                }
            }
        };

        btnLike.setOnClickListener(listener);
        btnHate.setOnClickListener(listener);
    }
}
