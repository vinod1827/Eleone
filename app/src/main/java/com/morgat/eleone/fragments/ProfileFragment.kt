package com.morgat.eleone.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.facebook.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.morgat.eleone.R
import com.morgat.eleone.activities.SeeFullImageActivity
import com.morgat.eleone.application.ElevenApp.Companion.preffs
import com.morgat.eleone.activities.MainMenuActivity
import com.morgat.eleone.models.ProfileApiResponse
import com.morgat.eleone.webservice.ApiRequest
import com.morgat.eleone.listeners.FragmentCallbackListener
import com.morgat.eleone.utils.Functions
import com.morgat.eleone.utils.Variables
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile_tab.*
import org.json.JSONException
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : RootFragment(), View.OnClickListener {

    private var adapter: ViewPagerAdapter? = null
    var isdataload = false
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        initializeViews()
        callApiForGetAllvideos()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.userImage -> openFullSizeImage(preffs?.userModel?.profileImageUrl)
            R.id.settingButton -> openSettings()
            R.id.followingLayout -> openFollowing()
            R.id.fansLayout -> openFollowers()
            R.id.backgroundImageView -> openFullSizeImage(preffs?.userModel?.backgroundImageUrl)
        }
    }

    /*  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
          super.setUserVisibleHint(isVisibleToUser)
          if (view != null && isVisibleToUser && !isdataload) {
              if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) initializeViews()
          }
          if (view != null && isVisibleToUser && isdataload) {
              callApiForGetAllvideos()
          }
      }*/

    private fun initializeViews() {
        backgroundImageView.setOnClickListener(this)
        userImage.setOnClickListener(this)
        settingButton.setOnClickListener(this)
        pager.offscreenPageLimit = 2
        adapter = ViewPagerAdapter(resources, childFragmentManager)
        pager.adapter = adapter
        profileTabLayout?.setupWithViewPager(pager)
        setupTabIcons()
        val observer = topLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height = topLayout.measuredHeight
                topLayout.viewTreeObserver.removeGlobalOnLayoutListener(
                        this)
                val observer = tabsMainLayout.viewTreeObserver
                observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val params = tabsMainLayout.layoutParams as RelativeLayout.LayoutParams
                        params.height = (tabsMainLayout.measuredHeight + height)
                        tabsMainLayout.layoutParams = params
                        tabsMainLayout.viewTreeObserver.removeGlobalOnLayoutListener(
                                this)
                    }
                })
            }
        })
        followingLayout.setOnClickListener(this)
        fansLayout.setOnClickListener(this)
        isdataload = true
        updateProfile()
    }

    private fun updateProfile() {
        userNameTextView.text = getString(R.string.fullname_text, preffs?.userModel?.firstName, preffs?.userModel?.lastName)
        try {
            Picasso.get().load(preffs?.userModel?.profileImageUrl)
                    .resize(200, 200)
                    .placeholder(R.drawable.profile_image_placeholder)
                    .centerCrop()
                    .into(userImage)
        } catch (e: Exception) {
        }
        try {
            Picasso.get().load(preffs?.userModel?.backgroundImageUrl)
                    .placeholder(R.drawable.tempbackground)
                    .into(backgroundImageView)
        } catch (e: Exception) {
        }
    }

    private fun setupTabIcons() {
        val view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        imageView1.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_my_video_color))
        profileTabLayout?.getTabAt(0)?.customView = view1
        val view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView2 = view2.findViewById<ImageView>(R.id.image)
        imageView2.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_liked_video_gray))
        profileTabLayout?.getTabAt(1)?.customView = view2
        profileTabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v?.findViewById<ImageView>(R.id.image)
                when (tab.position) {
                    0 -> {
                        if (UserVideoFragment.myvideo_count > 0) {
                            createPopupLayout!!.visibility = View.GONE
                        } else {
                            createPopupLayout!!.visibility = View.VISIBLE
                            val aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation)
                            createPopupLayout!!.startAnimation(aniRotate)
                        }
                        image?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_my_video_color))
                    }
                    1 -> {
                        createPopupLayout!!.clearAnimation()
                        createPopupLayout!!.visibility = View.GONE
                        image?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_liked_video_color))
                    }
                }
                tab.customView = v
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                when (tab.position) {
                    0 -> image.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_my_video_gray))
                    1 -> image.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_liked_video_gray))
                }
                tab.customView = v
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    internal inner class ViewPagerAdapter(private val resources: Resources, fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private var registeredFragments = SparseArray<Fragment>()
        override fun getItem(position: Int): Fragment {
            val result: Fragment
            when (position) {
                0 -> result = UserVideoFragment(preffs?.userModel?.id)
                1 -> result = LikedVideoFragment(preffs?.userModel?.id)
                else -> result = UserVideoFragment(preffs?.userModel?.id)
            }
            return result
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        }

        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        fun getRegisteredFragment(position: Int): Fragment {
            return registeredFragments[position]
        }

    }

    //this will get the all videos data of user and then parse the data
    private fun callApiForGetAllvideos() {
        val parameters = JSONObject()
        try {
            parameters.put("my_fb_id", preffs?.userModel?.id)
            parameters.put("fb_id", preffs?.userModel?.id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.callApi(context, Variables.showMyAllVideos, parameters) { resp -> parseData(resp) }
    }

    private fun parseData(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val data = msgArray.getJSONObject(0)
                val profileApiResponse = Gson().fromJson(data.toString(), ProfileApiResponse::class.java)

                userNameTextView.text = getString(R.string.fullname_text, profileApiResponse.userInfo?.firstName,
                        profileApiResponse.userInfo?.lastName)

                Picasso.get()
                        .load(profileApiResponse.userInfo?.profileImageUrl)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .resize(200, 200).centerCrop().into(userImage)

                if (profileApiResponse.userInfo?.backgroundImageUrl?.isNotEmpty() == true) {
                    Picasso.get()
                            .load(profileApiResponse.userInfo?.backgroundImageUrl)
                            .placeholder(R.drawable.tempbackground).into(backgroundImageView)
                } else {
                    Picasso.get()
                            .load(R.drawable.tempbackground).into(backgroundImageView)
                }


                followCountTextView.text = profileApiResponse.totalFollowing
                fanCountsTextView.text = profileApiResponse.totalFans
                heartCountTextView.text = profileApiResponse.totalLikes
                val userVideosObject = data.getJSONArray("user_videos")
                if (userVideosObject.toString() != "[" + "0" + "]") {
                    videoCountText.text = "${userVideosObject.length()} Videos"
                    createPopupLayout?.visibility = View.GONE
                } else {
                    createPopupLayout.visibility = View.VISIBLE
                    val aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation)
                    createPopupLayout.startAnimation(aniRotate)
                }
            } else {
                //Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun openSettings() {
        openMenuTab(settingButton)
    }

    private fun openEditProfile() {
        val editProfileFragment = EditProfileFragment(FragmentCallbackListener { updateProfile() })
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
        transaction.addToBackStack(null)
        transaction.replace(R.id.MainMenuFragment, editProfileFragment).commit()
    }

    //this method will get the big size of profile image.
    private fun openFullSizeImage(url: String?) {
        val seeImageF = SeeFullImageActivity()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        val args = Bundle()
        args.putSerializable("image_url", url)
        seeImageF.arguments = args
        transaction.addToBackStack(null)
        transaction.replace(R.id.MainMenuFragment, seeImageF).commit()
    }

    private fun openMenuTab(anchor_view: View?) {
        val wrapper: Context = ContextThemeWrapper(context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, anchor_view)
        popup.menuInflater.inflate(R.menu.menu, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_Profile_id -> openEditProfile()
                R.id.logout_id -> logout()
            }
            true
        }
    }

    private fun openFollowing() {
        val following_f = FollowingFragment(FragmentCallbackListener { callApiForGetAllvideos() })
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        val args = Bundle()
        args.putString("id", preffs?.userModel?.id)
        args.putString("from_where", "following")
        following_f.arguments = args
        transaction.addToBackStack(null)
        transaction.replace(R.id.MainMenuFragment, following_f).commit()
    }

    private fun openFollowers() {
        val following_f = FollowingFragment(FragmentCallbackListener { callApiForGetAllvideos() })
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        val args = Bundle()
        args.putString("id", preffs?.userModel?.id)
        args.putString("from_where", "fan")
        following_f.arguments = args
        transaction.addToBackStack(null)
        transaction.replace(R.id.MainMenuFragment, following_f).commit()
    }

    // this will erase all the user info store in locally and logout the user
    private fun logout() {
        preffs?.userModel = null
        requireActivity().finish()
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut();

        val delPermRequest = GraphRequest(AccessToken.getCurrentAccessToken(), "/{user-id}/permissions/", null,
                HttpMethod.DELETE, object : CallbackManager, GraphRequest.Callback {
            override fun onCompleted(graphResponse: GraphResponse?) {
                if (graphResponse != null) {
                    val error: FacebookRequestError = graphResponse.error
                    println("### ${error.errorMessage}")
                }
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
                return false
            }

        })
        delPermRequest.executeAsync();
        mGoogleSignInClient?.signOut()
        startActivity(Intent(activity, MainMenuActivity::class.java))
    }


    override fun onDetach() {
        super.onDetach()
        Functions.deleteCache(context)
    }
}