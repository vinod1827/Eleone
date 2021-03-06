package com.morgat.eleone.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.gson.Gson;
import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.models.UserModel;
import com.morgat.eleone.webservice.ApiRequest;
import com.morgat.eleone.listeners.CallbackListener;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.morgat.eleone.activities.ChatActivity;
import com.morgat.eleone.R;
import com.morgat.eleone.activities.SeeFullImageActivity;
import com.morgat.eleone.listeners.ApiCallbackListener;
import com.morgat.eleone.listeners.FragmentCallbackListener;
import com.morgat.eleone.utils.Functions;
import com.morgat.eleone.utils.Variables;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// This is the profile screen which is show in 5 tab as well as it is also call
// when we see the profile of other users

public class Profile_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;


    public TextView follow_unfollow_btn;
    public TextView username, video_count_txt;
    public ImageView imageView, backgroundImageView;
    public TextView follow_count_txt, fans_count_txt, heart_count_txt;

    ImageView back_btn, setting_btn;

    String user_id, user_name, user_pic;

    Bundle bundle;

    protected TabLayout tabLayout;

    protected ViewPager pager;

    private ViewPagerAdapter adapter;

    public boolean isdataload = false;


    RelativeLayout tabs_main_layout;

    LinearLayout top_layout;
    private UserModel userModel;


    public Profile_F() {

    }


    FragmentCallbackListener fragment_callbackListener;

    @SuppressLint("ValidFragment")
    public Profile_F(FragmentCallbackListener fragment_callbackListener) {
        this.fragment_callbackListener = fragment_callbackListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = getContext();


        bundle = getArguments();
        if (bundle != null) {
            user_id = bundle.getString("user_id");
            user_name = bundle.getString("user_name");
            user_pic = bundle.getString("user_pic");
        }


        return init();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userImage:
                if (userModel != null && !userModel.getProfileImageUrl().isEmpty())
                    openfullsizeImage(userModel.getProfileImageUrl());
                break;

            case R.id.backgroundImageView:
                if (userModel != null && !userModel.getBackgroundImageUrl().isEmpty())
                    openfullsizeImage(userModel.getBackgroundImageUrl());
                break;

            case R.id.followUnfollowTextView:

                if (ElevenApp.Companion.getPreffs().getUserModel() != null)
                    followUnFollowUser();
                else
                    Toast.makeText(context, "Please login in to app", Toast.LENGTH_SHORT).show();

                break;

            case R.id.settingButton:
                openSetting();
                break;

            case R.id.followingLayout:
                openFollowing();
                break;

            case R.id.fansLayout:
                openFollowers();
                break;

            case R.id.backButton:
                requireActivity().onBackPressed();
                break;
        }
    }


    public View init() {

        username = view.findViewById(R.id.userNameTextView);
        imageView = view.findViewById(R.id.userImage);
        backgroundImageView = view.findViewById(R.id.backgroundImageView);
        imageView.setOnClickListener(this);
        backgroundImageView.setOnClickListener(this);

        video_count_txt = view.findViewById(R.id.videoCountText);

        follow_count_txt = view.findViewById(R.id.followCountTextView);
        fans_count_txt = view.findViewById(R.id.fanCountsTextView);
        heart_count_txt = view.findViewById(R.id.heartCountTextView);


        setting_btn = view.findViewById(R.id.settingButton);
        setting_btn.setOnClickListener(this);

        back_btn = view.findViewById(R.id.backButton);
        back_btn.setOnClickListener(this);

        follow_unfollow_btn = view.findViewById(R.id.followUnfollowTextView);
        follow_unfollow_btn.setOnClickListener(this);


        tabLayout = (TabLayout) view.findViewById(R.id.profileTabLayout);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);

        adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        tabs_main_layout = view.findViewById(R.id.tabsMainLayout);
        top_layout = view.findViewById(R.id.topLayout);


        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height = (int) (tabs_main_layout.getMeasuredHeight() + height);
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        view.findViewById(R.id.followingLayout).setOnClickListener(this);
        view.findViewById(R.id.fansLayout).setOnClickListener(this);

        isdataload = true;


        callApiForGetAllvideos();


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (is_run_first_time) {

            callApiForGetAllvideos();

        }

    }

    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_color));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_gray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideoFragment(user_id);
                    break;
                case 1:
                    result = new LikedVideoFragment(user_id);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }


    boolean is_run_first_time = false;

    private void callApiForGetAllvideos() {

        if (bundle == null) {
            user_id = ElevenApp.Companion.getPreffs().getUserModel().getId();
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(context, Variables.showMyAllVideos, parameters, new CallbackListener() {
            @Override
            public void onResponse(String resp) {
                is_run_first_time = true;
                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                JSONObject data = msgArray.getJSONObject(0);
                JSONObject user_info = data.optJSONObject("user_info");

                userModel = new Gson().fromJson(user_info.toString(), UserModel.class);
                username.setText(getString(R.string.fullname_text, userModel.getFirstName(), userModel.getLastName()));

                if (!userModel.getProfileImageUrl().isEmpty()) {
                    Picasso.get()
                            .load(userModel.getProfileImageUrl())
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                            .resize(200, 200).centerCrop().into(imageView);
                } else {
                    Picasso.get()
                            .load(R.drawable.profile_image_placeholder)
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                            .resize(200, 200).centerCrop().into(imageView);
                }

                if (!userModel.getBackgroundImageUrl().isEmpty()) {
                    Picasso.get()
                            .load(userModel.getBackgroundImageUrl())
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                            .resize(200, 200).centerCrop().into(imageView);
                } else {
                    Picasso.get()
                            .load(R.drawable.tempbackground)
                            .placeholder(context.getResources().getDrawable(R.drawable.tempbackground))
                            .resize(200, 200).centerCrop().into(backgroundImageView);
                }


                follow_count_txt.setText(data.optString("total_following"));
                fans_count_txt.setText(data.optString("total_fans"));
                heart_count_txt.setText(data.optString("total_heart"));


                if (!data.optString("fb_id").
                        equals(ElevenApp.Companion.getPreffs().getUserModel().getId())) {

                    follow_unfollow_btn.setVisibility(View.VISIBLE);
                    JSONObject follow_Status = data.optJSONObject("follow_Status");
                    follow_unfollow_btn.setText(follow_Status.optString("follow_status_button"));
                    follow_status = follow_Status.optString("follow");
                }


                JSONArray user_videos = data.getJSONArray("user_videos");
                if (!user_videos.toString().equals("[" + "0" + "]")) {
                    video_count_txt.setText(user_videos.length() + " Videos");

                }


            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void openSetting() {

        Open_Chat_F();

    }


    public String follow_status = "0";

    public void followUnFollowUser() {

        final String send_status;
        if (follow_status.equals("0")) {
            send_status = "1";
        } else {
            send_status = "0";
        }

        Functions.Call_Api_For_Follow_or_unFollow(getActivity(),
                ElevenApp.Companion.getPreffs().getUserModel().getId(),
                user_id,
                send_status,
                new ApiCallbackListener() {
                    @Override
                    public void onResponseArrayReceived(ArrayList arrayList) {


                    }

                    @Override
                    public void onSuccess(String responce) {

                        if (send_status.equals("1")) {
                            follow_unfollow_btn.setText("UnFollow");
                            follow_status = "1";

                        } else if (send_status.equals("0")) {
                            follow_unfollow_btn.setText("Follow");
                            follow_status = "0";
                        }

                        callApiForGetAllvideos();
                    }

                    @Override
                    public void onFailure(String responce) {

                    }

                });


    }


    //this method will get the big size of profile image.
    public void openfullsizeImage(String url) {
        SeeFullImageActivity see_image_f = new SeeFullImageActivity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
        else
            transaction.replace(R.id.profileFrameLayout, see_image_f).commit();


    }


    public void Open_Chat_F() {

        ChatActivity chat_activity = new ChatActivity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("user_id", user_id);
        args.putString("user_name", user_name);
        args.putString("user_pic", user_pic);
        chat_activity.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
        else
            transaction.replace(R.id.profileFrameLayout, chat_activity).commit();


    }


    public void openFollowing() {

        FollowingFragment following_fragment = new FollowingFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "following");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);


        View view = getActivity().findViewById(R.id.MainMenuFragment);

        if (view != null)
            transaction.replace(R.id.MainMenuFragment, following_fragment).commit();
        else
            transaction.replace(R.id.profileFrameLayout, following_fragment).commit();


    }

    public void openFollowers() {
        FollowingFragment following_fragment = new FollowingFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "fan");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);
        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, following_fragment).commit();
        else
            transaction.replace(R.id.profileFrameLayout, following_fragment).commit();

    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (fragment_callbackListener != null)
            fragment_callbackListener.Responce(new Bundle());
        Functions.deleteCache(context);
    }
}
