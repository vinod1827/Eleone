package com.morgat.eleone.activities;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.InstanceIdResult;
import com.morgat.eleone.adapters.WatchVideosAdapter;
import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.webservice.ApiRequest;
import com.morgat.eleone.listeners.CallbackListener;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.morgat.eleone.fragments.CommentFragment;
import com.morgat.eleone.models.HomeModel;
import com.morgat.eleone.components.KeyboardHeightObserver;
import com.morgat.eleone.components.KeyboardHeightProvider;
import com.morgat.eleone.fragments.MainMenuFragment;
import com.morgat.eleone.fragments.Profile_F;
import com.morgat.eleone.R;
import com.morgat.eleone.listeners.ApiCallbackListener;
import com.morgat.eleone.listeners.FragmentCallbackListener;
import com.morgat.eleone.listeners.FragmentDataSentListener;
import com.morgat.eleone.utils.Functions;
import com.morgat.eleone.utils.Variables;
import com.morgat.eleone.fragments.TagedVideosFragment;
import com.morgat.eleone.fragments.VideoActionFragment;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.iid.FirebaseInstanceId;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class WatchVideosActivity extends AppCompatActivity implements Player.EventListener,
        KeyboardHeightObserver, View.OnClickListener, FragmentDataSentListener {

    Context context;

    RecyclerView recyclerView;
    ArrayList<HomeModel> data_list;
    int position = 0;
    int currentPage = -1;
    LinearLayoutManager layoutManager;

    WatchVideosAdapter adapter;

    ProgressBar p_bar;

    private KeyboardHeightProvider keyboardHeightProvider;

    RelativeLayout write_layout;


    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;


    String video_id;

    public WatchVideosActivity() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_watchvideo);
        context = this;

        p_bar = findViewById(R.id.progressBar);


        Intent bundle = getIntent();
        if (bundle != null) {

            Uri appLinkData = bundle.getData();

            if (appLinkData == null) {
                data_list = (ArrayList<HomeModel>) bundle.getSerializableExtra("arraylist");
                position = bundle.getIntExtra("position", 0);
                setAdapter();

            } else {
                String link = appLinkData.toString();
                String[] parts = link.split("=");
                video_id = parts[1];
                callApiForGetAllvideos(parts[1]);
            }
        }


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });


        write_layout = findViewById(R.id.write_layout);
        message_edit = findViewById(R.id.message_edit);
        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(this);

        send_progress = findViewById(R.id.send_progress);

        keyboardHeightProvider = new KeyboardHeightProvider(this);


        findViewById(R.id.WatchVideo_F).post(new Runnable() {
            public void run() {

                keyboardHeightProvider.start();

            }
        });

    }


    @Override
    public void onBackPressed() {

        if (video_id != null) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }

    }

    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllvideos(final String id) {


        if (ElevenApp.Companion.getPreffs().getFirebasetoken() == null){
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            ElevenApp.Companion.getPreffs().setFirebasetoken(token);

                            JSONObject parameters = new JSONObject();
                            try {
                                parameters.put("fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
                                parameters.put("token", ElevenApp.Companion.getPreffs().getFirebasetoken());
                                parameters.put("video_id", id);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            ApiRequest.callApi(context, Variables.showAllVideos, parameters, new CallbackListener() {
                                @Override
                                public void onResponse(String resp) {
                                    parseData(resp);
                                }
                            });
                        }
                    });
        }else{
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
                parameters.put("token", ElevenApp.Companion.getPreffs().getFirebasetoken());
                parameters.put("video_id", id);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            ApiRequest.callApi(context, Variables.showAllVideos, parameters, new CallbackListener() {
                @Override
                public void onResponse(String resp) {
                    parseData(resp);
                }
            });
        }
    }

    public void parseData(String responce) {

        data_list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    HomeModel item = new HomeModel();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");


                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = Variables.base_url + itemdata.optString("video");
                    item.video_description = itemdata.optString("description");

                    item.thum = Variables.base_url + itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");

                    data_list.add(item);
                }

                setAdapter();

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    private void callApiForSinglevideos(final int postion) {

        try {
            JSONObject parameters = new JSONObject();

            parameters.put("fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
            parameters.put("token", ElevenApp.Companion.getPreffs().getFirebasetoken());
            parameters.put("video_id", data_list.get(postion).video_id);


            ApiRequest.callApi(context, Variables.showAllVideos, parameters, new CallbackListener() {
                @Override
                public void onResponse(String resp) {
                    singalVideoParseData(postion, resp);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public void singalVideoParseData(int pos, String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    HomeModel item = new HomeModel();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");


                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = Variables.base_url + itemdata.optString("video");
                    item.video_description = itemdata.optString("description");

                    item.thum = Variables.base_url + itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");

                    data_list.remove(pos);
                    data_list.add(pos, item);
                    adapter.notifyDataSetChanged();
                }


            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    public void setAdapter() {
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        adapter = new WatchVideosAdapter(context, data_list, new WatchVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final HomeModel item, View view) {

                switch (view.getId()) {

                    case R.id.moreOption:
                        showVideoOption(item);
                        break;
                    case R.id.user_pic:
                        onPause();
                        openProfile(item, false);
                        break;

                    case R.id.like_layout:
                        if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                            likeVideo(postion, item);
                        } else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.comment_layout:
                        openComment(item);
                        break;

                    case R.id.shared_layout:
                        File file = new File(Variables.getRootPath(WatchVideosActivity.this) + item.video_id + ".mp4");
                        if (file.exists()) {
                            shareFile(file.getAbsolutePath());
                        } else {
                            saveVideo(item);
                        }
                        break;


                    case R.id.sound_image_layout:
                        if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                            if (check_permissions()) {
                                Intent intent = new Intent(WatchVideosActivity.this, VideoSoundActivity.class);
                                intent.putExtra("data", item);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);


        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    currentPage = page_no;

                    previousPlayer();
                    setPlayer(currentPage);
                }

            }
        });

        recyclerView.scrollToPosition(position);

    }


    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }


    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        Log.d("resp", "" + height);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(write_layout.getWidth(), write_layout.getHeight());
        params.bottomMargin = height;
        write_layout.setLayoutParams(params);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                if (ElevenApp.Companion.getPreffs().getUserModel() != null) {

                    String comment_txt = message_edit.getText().toString();
                    if (!TextUtils.isEmpty(comment_txt)) {
                        sendComments(data_list.get(currentPage).fb_id, data_list.get(currentPage).video_id, comment_txt);
                    }


                } else {
                    Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onDataSent(String yourData) {
        int comment_count = Integer.parseInt(yourData);
        HomeModel item = data_list.get(currentPage);
        item.video_comment_count = "" + comment_count;
        data_list.add(currentPage, item);
        adapter.notifyDataSetChanged();
    }


    public void setPlayer(final int currentPage) {

        final HomeModel item = data_list.get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(item.video_url));

        Log.d(Variables.tag, item.video_url);


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);


        View layout = layoutManager.findViewByPosition(currentPage);
        PlayerView playerView = layout.findViewById(R.id.playerview);
        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
        privious_player = player;


        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if ((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if (deltaX > 0) {
                            openProfile(item, true);
                        }
                    }


                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    } else {
                        privious_player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    showVideoOption(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    }

                    if (ElevenApp.Companion.getPreffs().getUserModel() != null) {

                        showHeartOnDoubleTap(item, mainlayout, e);
                        likeVideo(currentPage, item);

                    } else {
                        Toast.makeText(context, "Please Login into ", Toast.LENGTH_SHORT).show();
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        TextView desc_txt = layout.findViewById(R.id.desc_txt);
        HashTagHelper.Creator.create(context.getResources().getColor(R.color.maincolor), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);


        LinearLayout soundimage = (LinearLayout) layout.findViewById(R.id.sound_image_layout);
        Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
        soundimage.startAnimation(aniRotate);

        if (ElevenApp.Companion.getPreffs().getUserModel() != null)
            Functions.callApiForUpdateView(WatchVideosActivity.this, item.video_id);


        callApiForSinglevideos(currentPage);
    }


    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;

    public void previousPlayer() {
        if (privious_player != null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }


    public void showHeartOnDoubleTap(HomeModel item, final RelativeLayout mainlayout, MotionEvent e) {

        int x = (int) e.getX() - 100;
        int y = (int) e.getY() - 100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if (item.liked.equals("1"))
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like_fill));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context, R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeoutani);

    }


    // this function will call for like the video and Call an Api for like the video
    public void likeVideo(final int position, final HomeModel home_model) {

        String action = home_model.liked;

        if (action.equals("1")) {
            action = "0";
            home_model.like_count = "" + (Integer.parseInt(home_model.like_count) - 1);
        } else {
            action = "1";
            home_model.like_count = "" + (Integer.parseInt(home_model.like_count) + 1);
        }


        data_list.remove(position);
        home_model.liked = action;
        data_list.add(position, home_model);
        adapter.notifyDataSetChanged();


        Functions.callApiForLikeVideo(this, home_model.video_id, action, new ApiCallbackListener() {

            @Override
            public void onResponseArrayReceived(ArrayList arrayList) {

            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });
    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 2);
        } else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // this will open the comment screen
    public void openComment(HomeModel item) {
        int comment_count = Integer.parseInt(item.video_comment_count);
        FragmentDataSentListener fragment_data_sentListener = this;

        CommentFragment comment_fragment = new CommentFragment(comment_count, fragment_data_sentListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.fb_id);
        comment_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, comment_fragment).commit();

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(HomeModel item, boolean from_right_to_left) {

        if (ElevenApp.Companion.getPreffs().getUserModel().getId().equals(item.fb_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F(new FragmentCallbackListener() {
                @Override
                public void Responce(Bundle bundle) {

                    callApiForSinglevideos(currentPage);

                }
            });
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (from_right_to_left)
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name", item.first_name + " " + item.last_name);
            args.putString("user_pic", item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.WatchVideo_F, profile_f).commit();

        }


    }


    public void sendComments(final String user_id, String video_id, final String comment) {

        send_progress.setVisibility(View.VISIBLE);
        send_btn.setVisibility(View.GONE);

        Functions.callApiForSendComment(this, video_id, comment, new ApiCallbackListener() {
            @Override
            public void onResponseArrayReceived(ArrayList arrayList) {

                message_edit.setText(null);
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);

                int comment_count = Integer.parseInt(data_list.get(currentPage).video_comment_count);
                comment_count++;
                onDataSent("" + comment_count);


            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });

        SendPushNotification(user_id, comment);
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

        TagedVideosFragment taged_videos_fragment = new TagedVideosFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, taged_videos_fragment).commit();

    }


    CharSequence[] options;

    private void showVideoOption(final HomeModel home_model) {

        options = new CharSequence[]{"Save Video", "Cancel"};

        if (home_model.fb_id.equals(ElevenApp.Companion.getPreffs().getUserModel().getId()))
            options = new CharSequence[]{"Save Video", "Delete Video", "Cancel"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video")) {
                    if (Functions.Checkstoragepermision(WatchVideosActivity.this))
                        saveVideo(home_model);

                } else if (options[item].equals("Delete Video")) {

                    Functions.showLoader(WatchVideosActivity.this, false, false);
                    Functions.Call_Api_For_Delete_Video(WatchVideosActivity.this, home_model.video_id, new ApiCallbackListener() {
                        @Override
                        public void onResponseArrayReceived(ArrayList arrayList) {

                        }

                        @Override
                        public void onSuccess(String responce) {

                            Functions.cancelLoader();
                            setResult(Activity.RESULT_OK);
                            finish();

                        }

                        @Override
                        public void onFailure(String responce) {

                        }
                    });

                } else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public void saveVideo(final HomeModel item) {

        Functions.showDeterminentLoader(context, false, false);
        PRDownloader.initialize(getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.getRootPath(context), item.video_id + "no_watermark" + ".mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                        Functions.showLoadingProgress(prog / 2);

                    }
                });


        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                applywatermark(item);
            }

            @Override
            public void onError(Error error) {
                Delete_file_no_watermark(item);
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                Functions.cancel_determinent_loader();
            }


        });


    }

    public void applywatermark(final HomeModel item) {

        Bitmap myLogo = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_watermark_image)).getBitmap();
        Bitmap bitmap_resize = Bitmap.createScaledBitmap(myLogo, 100, 100, false);
        GlWatermarkFilter filter = new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Variables.getRootPath(this) + item.video_id + "no_watermark" + ".mp4",
                Variables.getRootPath(this) + item.video_id + ".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp", "" + (int) (progress * 100));
                        Functions.showLoadingProgress((int) ((progress * 100) / 2) + 50);

                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();
                                Delete_file_no_watermark(item);
                                scanFile(item);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp", exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Delete_file_no_watermark(item);
                                    Functions.cancel_determinent_loader();
                                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(HomeModel item) {
        File file = new File(Variables.getRootPath(this) + item.video_id + "no_watermark" + ".mp4");
        if (file.exists()) {
            file.delete();
        }
    }

    public void scanFile(HomeModel item) {
        MediaScannerConnection.scanFile(WatchVideosActivity.this,
                new String[]{Variables.getRootPath(this) + item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        shareFile(path);
                    }
                });
    }

    private void shareFile(String path) {
        final VideoActionFragment fragment = new VideoActionFragment(path, new FragmentCallbackListener() {
            @Override
            public void Responce(Bundle bundle) {

                if (bundle.getString("action").equals("save")) {
                    //saveVideo(item);
                }
            }
        });
        fragment.show(getSupportFragmentManager(), "");
    }


    public void SendPushNotification(String user_id, String comment) {

        JSONObject notimap = new JSONObject();
        try {
            notimap.put("title", ElevenApp.Companion.getPreffs().getUserModel().getUsername() + " Commented on your video");
            notimap.put("message", comment);
            notimap.put("icon", ElevenApp.Companion.getPreffs().getUserModel().getProfileImageUrl());
            notimap.put("senderid", ElevenApp.Companion.getPreffs().getUserModel().getId());
            notimap.put("receiverid", user_id);
            notimap.put("action_type", "comment");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.sendPushNotification, notimap, null);

    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onPause() {
        super.onPause();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (privious_player != null) {
            privious_player.release();
        }

        keyboardHeightProvider.close();
    }


    // Bottom all the function and the Call back listener of the Expo player
    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            p_bar.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            p_bar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    @Override
    public void onSeekProcessed() {


    }


}
