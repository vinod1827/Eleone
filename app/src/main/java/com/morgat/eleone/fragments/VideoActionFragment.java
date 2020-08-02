package com.morgat.eleone.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.morgat.eleone.R;
import com.morgat.eleone.adapters.VideoSharingApps_Adapter;
import com.morgat.eleone.listeners.FragmentCallbackListener;
import com.morgat.eleone.utils.Variables;

import java.io.File;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoActionFragment extends BottomSheetDialogFragment {

    View view;
    Context context;
    RecyclerView recyclerView;

    FragmentCallbackListener fragment_callbackListener;

    String videoPath;

    ProgressBar progressBar;

    public VideoActionFragment() {
    }

    @SuppressLint("ValidFragment")
    public VideoActionFragment(String videoPath, FragmentCallbackListener fragment_callbackListener) {
        this.videoPath = videoPath;
        this.fragment_callbackListener = fragment_callbackListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_action, container, false);
        context = getContext();

        progressBar = view.findViewById(R.id.progress_bar);
        // view.findViewById(R.id.save_video_layout).setOnClickListener(this);
        //  view.findViewById(R.id.copy_layout).setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Get_Shared_app();
            }
        }, 1000);

        return view;
    }

    VideoSharingApps_Adapter adapter;

    public void Get_Shared_app() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    PackageManager pm = getActivity().getPackageManager();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://google.com");

                    List<ResolveInfo> launchables = pm.queryIntentActivities(intent, 0);

                    for (int i = 0; i < launchables.size(); i++) {

                        if (launchables.get(i).activityInfo.name.contains("SendTextToClipboardActivity")) {
                            launchables.remove(i);
                            break;
                        }

                    }

                    Collections.sort(launchables,
                            new ResolveInfo.DisplayNameComparator(pm));

                    adapter = new VideoSharingApps_Adapter(context, launchables, new VideoSharingApps_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int positon, ResolveInfo item, View view) {
                            Toast.makeText(context, "" + item.activityInfo.name, Toast.LENGTH_SHORT).show();
                            Open_App(item);
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                } catch (Exception e) {

                }
            }
        }).start();


    }


    public void Open_App(ResolveInfo resolveInfo) {
        try {

            /*ActivityInfo activity = resolveInfo.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent i = new Intent(Intent.ACTION_MAIN);

            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);*/

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            /*Uri uri =FileProvider.getUriForFile(
                    requireActivity(),
                    "com.morgat.eleone.provider", //(use your app signature + ".provider" )
                    new File(videoPath));*/


            Uri uri = Uri.fromFile(new File(videoPath));
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            //intent.setType("text/plain");
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            // intent.setComponent(name);
            startActivity(intent);
        } catch (Exception e) {
            if (Variables.IS_DEBUG)
            System.out.println("### " + e.getMessage());
        }
    }

    /*public void shareVideo(String pkgname, String appname) {
        String path = null;
        try {
            path = MediaStore.Images.Media.insertImage(getContentResolver(),
                    arrImagePath.get(slidePager.getCurrentItem()), "Title", null);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        Uri uri = Uri.parse(path);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage(pkgname);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("Video/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File");
    }*/

   /* @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_video_layout:

                if (Functions.Checkstoragepermision(getActivity())) {

                    Bundle bundle = new Bundle();
                    bundle.putString("action", "save");
                    dismiss();
                    fragment_callback.Responce(bundle);
                }

                break;

            case R.id.copy_layout:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", "http://bringthings.com/API/tictic/view.php?id=" + videoPath);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Link Copy in clipboard", Toast.LENGTH_SHORT).show();
                break;
        }
    }*/


}
