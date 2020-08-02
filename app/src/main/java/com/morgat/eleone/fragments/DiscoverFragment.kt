package com.morgat.eleone.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.morgat.eleone.R
import com.morgat.eleone.application.ElevenApp.Companion.preffs
import com.morgat.eleone.models.HomeModel
import com.morgat.eleone.models.DiscoverModel
import com.morgat.eleone.webservice.ApiRequest
import com.morgat.eleone.utils.Variables
import com.morgat.eleone.activities.WatchVideosActivity
import com.morgat.eleone.adapters.DiscoverAdapter
import kotlinx.android.synthetic.main.fragment_discover.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DiscoverFragment : RootFragment() {
    var datalist: ArrayList<DiscoverModel>? = null
    var adapter: DiscoverAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        callApiForGetAllvideos()
    }

    private fun initializeViews() {
        datalist = ArrayList()
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = DiscoverAdapter(context, datalist, DiscoverAdapter.OnItemClickListener { datalist, postion -> OpenWatchVideo(postion, datalist) })
        recyclerView.adapter = adapter
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val query = searchEditText.getText().toString()
                if (adapter != null) adapter?.filter?.filter(query)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        swipeRefreshLayout.setColorSchemeResources(R.color.black)
        swipeRefreshLayout.setOnRefreshListener { callApiForGetAllvideos() }
    }

    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab
    private fun callApiForGetAllvideos() {
        val parameters = JSONObject()
        try {
            parameters.put("fb_id", preffs?.userModel?.id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.callApi(context, Variables.discover, parameters) { resp ->
            parseData(resp)
            swipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun parseData(response: String?) {
        datalist!!.clear()
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                for (d in 0 until msgArray.length()) {
                    val discover_model = DiscoverModel()
                    val discover_object = msgArray.optJSONObject(d)
                    discover_model.title = discover_object.optString("section_name")
                    val video_array = discover_object.optJSONArray("sections_videos")
                    val video_list = ArrayList<HomeModel>()
                    for (i in 0 until video_array.length()) {
                        val itemdata = video_array.optJSONObject(i)
                        val item = HomeModel()
                        val user_info = itemdata.optJSONObject("user_info")
                        item.fb_id = user_info.optString("fb_id")
                        item.first_name = user_info.optString("first_name")
                        item.last_name = user_info.optString("last_name")
                        item.profile_pic = user_info.optString("profile_pic")
                        val count = itemdata.optJSONObject("count")
                        item.like_count = count.optString("like_count")
                        item.video_comment_count = count.optString("video_comment_count")
                        val sound_data = itemdata.optJSONObject("sound")
                        item.sound_id = sound_data.optString("id")
                        item.sound_name = sound_data.optString("sound_name")
                        item.sound_pic = sound_data.optString("thum")
                        item.video_id = itemdata.optString("id")
                        item.liked = itemdata.optString("liked")
                        item.video_url = Variables.base_url + itemdata.optString("video")
                        item.thum = Variables.base_url + itemdata.optString("thum")
                        item.gif = Variables.base_url + itemdata.optString("gif")
                        item.created_date = itemdata.optString("created")
                        item.video_description = itemdata.optString("description")
                        video_list.add(item)
                    }
                    discover_model.arrayList = video_list
                    datalist!!.add(discover_model)
                }
                adapter!!.notifyDataSetChanged()
            } else {
                //Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    // When you click on any Video a new activity is open which will play the Clicked video
    private fun OpenWatchVideo(postion: Int, data_list: ArrayList<HomeModel>) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        intent.putExtra("arraylist", data_list)
        intent.putExtra("position", postion)
        startActivity(intent)
    }
}