package com.morgat.eleone.fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.morgat.eleone.adapters.HomeAdapter;
import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.models.HomeModel;
import com.morgat.eleone.webservice.ApiRequest;
import com.morgat.eleone.listeners.CallbackListener;
import com.morgat.eleone.activities.VideoSoundActivity;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.morgat.eleone.R;
import com.morgat.eleone.listeners.ApiCallbackListener;
import com.morgat.eleone.listeners.FragmentCallbackListener;
import com.morgat.eleone.listeners.FragmentDataSentListener;
import com.morgat.eleone.utils.Functions;
import com.morgat.eleone.utils.Variables;
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
/*
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
*/
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */

// this is the main view which is show all  the video in list
public class HomeFragment extends RootFragment implements Player.EventListener, FragmentDataSentListener {

    View view;
    Context context;


    RecyclerView recyclerView;
    ArrayList<HomeModel> data_list;
    int currentPage = -1;
    LinearLayoutManager layoutManager;

    ProgressBar p_bar;

    SwipeRefreshLayout swiperefresh;

    public HomeFragment() {
        // Required empty public constructor
    }

    int swipe_count = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();

        p_bar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


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

                    releasePreviousPlayer();
                    setPlayer(currentPage);

                }
            }
        });


        swiperefresh = view.findViewById(R.id.swipeRefreshLayout);
        swiperefresh.setProgressViewOffset(false, 0, 200);

        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = -1;
                callApiForGetAllvideos();
            }
        });

        callApiForGetAllvideos();

        Load_add();

        return view;
    }


    // InterstitialAd mInterstitialAd;
    public void Load_add() {

        // this is test app id you will get the actual id when you add app in your
        //add mob account
        /*MobileAds.initialize(context,
                getResources().getString(R.string.ad_app_id));


        //code for intertial add
        mInterstitialAd = new InterstitialAd(context);

        //here we will get the add id keep in mind above id is app id and below Id is add Id
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.my_Interstitial_Add));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });*/


    }


    boolean is_add_show = false;
    HomeAdapter adapter;

    public void Set_Adapter() {

        adapter = new HomeAdapter(context, data_list, new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final HomeModel item, View view) {

                switch (view.getId()) {

                    case R.id.user_pic:

                    case R.id.userNameTextView:
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
                        is_add_show = false;
                        File file = new File(Variables.getRootPath(requireActivity()) + item.video_id + ".mp4");
                        if (file.exists()) {
                            shareFile(file.getAbsolutePath());
                        } else {
                            saveVideo(item);
                        }

                        break;


                    case R.id.sound_image_layout:
                        if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                            if (check_permissions()) {
                                Intent intent = new Intent(getActivity(), VideoSoundActivity.class);
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
        fragment.show(getChildFragmentManager(), "");
    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllvideos() {

        JSONObject parameters = new JSONObject();
        try {
            if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                parameters.put("fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
            } else {
                parameters.put("fb_id", "0");
            }

            parameters.put("token", ElevenApp.Companion.getPreffs().getFirebasetoken());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.showAllVideos, parameters, new CallbackListener() {
            @Override
            public void onResponse(String resp) {
                swiperefresh.setRefreshing(false);
                parseData(resp);
            }
        });
    }

    public void parseData(String response) {

        data_list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(response);
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

                Set_Adapter();

            } else {
                // Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void callApiForSinglevideos(final int postion) {


        JSONObject parameters = new JSONObject();
        try {
            if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                parameters.put("fb_id", ElevenApp.Companion.getPreffs().getUserModel().getId());
            } else {
                parameters.put("fb_id", "0");
            }

            if (ElevenApp.Companion.getPreffs().getFirebasetoken() != null)
                parameters.put("token", ElevenApp.Companion.getPreffs().getFirebasetoken());
            else
                parameters.put("token", "Null");
            parameters.put("video_id", data_list.get(postion).video_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(context, Variables.showAllVideos, parameters, new CallbackListener() {
            @Override
            public void onResponse(String resp) {
                swiperefresh.setRefreshing(false);
                singalVideoParseData(postion, resp);
            }
        });


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
                //Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    // this will call when swipe for another video and
    // this function will set the player to the current video
    public void setPlayer(final int currentPage) {

        final HomeModel item = data_list.get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(item.video_url));

        Log.d("resp", item.video_url);


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);


        View layout = layoutManager.findViewByPosition(currentPage);
        final PlayerView playerView = layout.findViewById(R.id.playerview);
        playerView.setPlayer(player);


        player.setPlayWhenReady(is_visible_to_user);
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
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    }


                    if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                        Show_heart_on_DoubleTap(item, mainlayout, e);
                        likeVideo(currentPage, item);
                    } else {
                        Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
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

                onPause();
                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);


        LinearLayout soundimage = layout.findViewById(R.id.sound_image_layout);
        Animation sound_animation = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
        soundimage.startAnimation(sound_animation);

        if (ElevenApp.Companion.getPreffs().getUserModel() != null)
            Functions.callApiForUpdateView(getActivity(), item.video_id);


        swipe_count++;
       /* if(swipe_count>4){
            //Show_add();
            swipe_count=0;
        }*/


        callApiForSinglevideos(currentPage);

    }


    public void Show_heart_on_DoubleTap(HomeModel item, final RelativeLayout mainlayout, MotionEvent e) {

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


/*
    public void Show_add(){
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }*/


    @Override
    public void onDataSent(String yourData) {
        int comment_count = Integer.parseInt(yourData);
        HomeModel item = data_list.get(currentPage);
        item.video_comment_count = "" + comment_count;
        data_list.remove(currentPage);
        data_list.add(currentPage, item);
        adapter.notifyDataSetChanged();
    }


    // this will call when go to the home tab From other tab.
    // this is very importent when for video play and pause when the focus is changes
    boolean is_visible_to_user;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        is_visible_to_user = isVisibleToUser;

        if (privious_player != null && isVisibleToUser) {
            privious_player.setPlayWhenReady(true);
        } else if (privious_player != null && !isVisibleToUser) {
            privious_player.setPlayWhenReady(false);
        }
    }


    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;

    public void releasePreviousPlayer() {
        if (privious_player != null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
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

        Functions.callApiForLikeVideo(getActivity(), home_model.video_id, action, new ApiCallbackListener() {

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


    // this will open the comment screen
    private void openComment(HomeModel item) {

        int comment_counnt = Integer.parseInt(item.video_comment_count);

        FragmentDataSentListener fragment_data_sentListener = this;

        CommentFragment comment_fragment = new CommentFragment(comment_counnt, fragment_data_sentListener);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.fb_id);
        comment_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, comment_fragment).commit();


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
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
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
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

        TagedVideosFragment taged_videos_fragment = new TagedVideosFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, taged_videos_fragment).commit();


    }


    private void Show_video_option(final HomeModel home_model) {

        final CharSequence[] options = {"Save Video", "Cancel"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video")) {
                    if (Functions.Checkstoragepermision(getActivity()))
                        saveVideo(home_model);

                } else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public void saveVideo(final HomeModel item) {

        Functions.showDeterminentLoader(context, false, false);
        PRDownloader.initialize(getActivity().getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.getRootPath(requireActivity()), item.video_id + "no_watermark" + ".mp4")
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
                Toast.makeText(context, error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                Functions.cancel_determinent_loader();
            }


        });


    }

    public void applywatermark(final HomeModel item) {

        Bitmap myLogo = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_watermark_image)).getBitmap();
        Bitmap bitmap_resize = Bitmap.createScaledBitmap(myLogo, 100, 100, false);
        GlWatermarkFilter filter = new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Variables.getRootPath(requireActivity()) + item.video_id + "no_watermark" + ".mp4",
                Variables.getRootPath(requireActivity()) + item.video_id + ".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp", "" + (int) (progress * 100));
                        Functions.showLoadingProgress((int) ((progress * 100) / 2) + 50);

                    }

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
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

                        getActivity().runOnUiThread(new Runnable() {
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
        File file = new File(Variables.getRootPath(requireActivity()) + item.video_id + "no_watermark" + ".mp4");
        if (file.exists()) {
            file.delete();
        }
    }

    private void scanFile(final HomeModel item) {
        MediaScannerConnection.scanFile(getActivity(),
                new String[]{Variables.getRootPath(requireActivity()) + item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        if (Variables.IS_DEBUG) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            System.out.println("####################  Path -> " + path);
                            System.out.println("####################  URI -> " + uri);
                        }
                        shareFile(path);
                    }
                });
    }


    public boolean is_fragment_exits() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            return false;
        } else {
            return true;
        }

    }

    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onResume() {
        super.onResume();
        if ((privious_player != null && is_visible_to_user) && !is_fragment_exits()) {
            privious_player.setPlayWhenReady(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
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
    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
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
