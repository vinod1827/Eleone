package com.morgat.eleone.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.morgat.eleone.adapters.CommentsAdapter;
import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.fragments.RootFragment;
import com.morgat.eleone.R;
import com.morgat.eleone.models.CommentModel;
import com.morgat.eleone.listeners.ApiCallbackListener;
import com.morgat.eleone.webservice.ApiRequest;
import com.morgat.eleone.listeners.FragmentDataSentListener;
import com.morgat.eleone.utils.Functions;
import com.morgat.eleone.utils.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;

    CommentsAdapter adapter;

    ArrayList<CommentModel> data_list;

    String video_id;
    String user_id;

    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    TextView comment_count_txt;

    FrameLayout comment_screen;

    public static int comment_count = 0;

    public CommentFragment() {

    }

    FragmentDataSentListener fragment_data_sentListener;

    @SuppressLint("ValidFragment")
    public CommentFragment(int count, FragmentDataSentListener fragment_data_sentListener) {
        comment_count = count;
        this.fragment_data_sentListener = fragment_data_sentListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();


        comment_screen = view.findViewById(R.id.comment_screen);
        comment_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();

            }
        });

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });


        Bundle bundle = getArguments();
        if (bundle != null) {
            video_id = bundle.getString("video_id");
            user_id = bundle.getString("user_id");
        }


        comment_count_txt = view.findViewById(R.id.comment_count);

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);


        data_list = new ArrayList<>();
        adapter = new CommentsAdapter(context, data_list, new CommentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, CommentModel item, View view) {


            }
        });

        recyclerView.setAdapter(adapter);


        message_edit = view.findViewById(R.id.message_edit);


        send_progress = view.findViewById(R.id.send_progress);
        send_btn = view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = message_edit.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    if (ElevenApp.Companion.getPreffs().getUserModel() != null) {
                        Send_Comments(video_id, message);
                        message_edit.setText(null);
                        send_progress.setVisibility(View.VISIBLE);
                        send_btn.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "Please Login into the app", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        Get_All_Comments();


        return view;
    }


    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());

        super.onDetach();
    }

    // this funtion will get all the comments against post
    public void Get_All_Comments() {

        Functions.Call_Api_For_get_Comment(getActivity(), video_id, new ApiCallbackListener() {
            @Override
            public void onResponseArrayReceived(ArrayList arrayList) {
                ArrayList<CommentModel> arrayList1 = arrayList;
                for (CommentModel item : arrayList1) {
                    data_list.add(item);
                }
                comment_count_txt.setText(data_list.size() + " comments");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }

        });

    }


    // this function will call an api to upload your comment
    public void Send_Comments(String video_id, final String comment) {

        Functions.callApiForSendComment(getActivity(), video_id, comment, new ApiCallbackListener() {
            @Override
            public void onResponseArrayReceived(ArrayList arrayList) {

                ArrayList<CommentModel> arrayList1 = arrayList;
                for (CommentModel item : arrayList1) {
                    data_list.add(0, item);
                    comment_count++;

                    SendPushNotification(getActivity(), user_id, comment);

                    comment_count_txt.setText(comment_count + " comments");

                    if (fragment_data_sentListener != null)
                        fragment_data_sentListener.onDataSent("" + comment_count);

                }
                adapter.notifyDataSetChanged();
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });

    }


    public void SendPushNotification(Activity activity, String user_id, String comment) {

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


}
