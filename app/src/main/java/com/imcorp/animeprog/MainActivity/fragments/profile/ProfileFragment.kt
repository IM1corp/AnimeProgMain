package com.imcorp.animeprog.MainActivity.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelHome
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelProfile
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile.YummyUser
import com.imcorp.animeprog.databinding.FragmentProfileBinding

class ProfileFragment : SimpleFragment<FragmentProfileBinding>(R.layout.fragment_profile, buildT = { FragmentProfileBinding.bind(it)}) {
    internal var profile: YummyUser? = null; private set(it){field=it; ProfileDataUpdater(this).update()}
    private val authed get() = !activity.dataBase.settings.yummyToken.isNullOrEmpty()
    private val values = arrayOf(OneMenuButton(R.string.settings) {
        goToAction(ProfileFragmentDirections.actionProfileFragmentToSettingsFragment())
    }, OneMenuButton(R.string.history) {
        goToAction(ProfileFragmentDirections.actionProfileFragmentToPageHistoryFragment())
    })
    override val viewModel: SaveStateModelAbs by navGraphViewModels<SaveStateModelProfile>(R.id.nav_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        updateMenuSelectionItem(activity as MainActivity, R.id.profileFragment)
        // Inflate the layout for this fragment
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            if(containsKey(Config.GO_TO_FRAGMENT)){
                if(getInt(Config.GO_TO_FRAGMENT) == R.id.loginFragment)
                    goToAction(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
                else {
                    val goTo = when (getInt(Config.GO_TO_FRAGMENT)) {
                        R.id.settingsFragment -> 0
                        R.id.pageHistoryFragment -> 1
                        else -> values.size - 1
                    }
                    values[goTo].onItemClick.run()
                }
                remove(Config.GO_TO_FRAGMENT)
                onPause()
            }
        }
        viewBinding.myActionButtons.adapter = ArrayAdapter(this.requireActivity(), android.R.layout.simple_list_item_1, values.map{getString(it.stringId)})
        viewBinding.myActionButtons.setOnItemClickListener { _, _, position, _ -> values[position].onItemClick.run() }
        viewBinding.authButton.setOnClickListener{
            goToAction(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
        }
    }
    override fun saveInstanceState(outState: Bundle) {
        super.saveInstanceState(outState)
        outState.putParcelable(Config.PROFILE_KEY, profile)
    }
    override fun restoreInstanceState(outState: Bundle, hostChanged: Boolean) {
        if(hostChanged){
            if(authed)
                startLoading()
            else
                updateProfileVisibility(ProfileVisibility.NOT_AUTHED)

        }
        else {
            profile = outState.getParcelable(Config.PROFILE_KEY)
            updateProfileVisibility(ProfileVisibility.AUTHED)
        }
    }

    override fun initializeState(restore: Boolean) {
        if(restore) return
        if(authed){
            profile = null
            startLoading()
        }
        else
            updateProfileVisibility(ProfileVisibility.NOT_AUTHED)
    }

    private fun updateProfileVisibility(visibility: ProfileVisibility=ProfileVisibility.NOT_AUTHED, error: String? = null) {
        viewBinding.authButton.visibility = if(visibility == ProfileVisibility.NOT_AUTHED ) View.VISIBLE else View.INVISIBLE
        if(visibility == ProfileVisibility.ERROR){
            viewBinding.retryButton.visibility = View.VISIBLE
            viewBinding.errorTextView.text = error
            viewBinding.errorTextView.visibility = View.VISIBLE
        }
        else viewBinding.retryButton.visibility = View.INVISIBLE.also{viewBinding.errorTextView.visibility=it}

    }


    override fun onResume() {
        super.onResume()
        (context as? MainActivity)?.binding?.FAB?.visibility = View.VISIBLE
    }
    override fun onPause() {
        super.onPause()
        (context as? MainActivity)?.binding?.FAB?.visibility = View.GONE
    }

    private fun goToAction(action: NavDirections) = findNavController().navigate(action)

    private var loadingThread: Thread? = null
    fun startLoading(){
        updateProfileVisibility(ProfileVisibility.LOADING)

        loadingThread?.interrupt()
        loadingThread = Thread{
            (getActivity() as? MyApp)?.run{
                try {
                    val user: YummyUser = request?.getProfile(dataBase.settings.mainHost)!!
                    threadCallback?.post {
                        profile = user
                        updateProfileVisibility(ProfileVisibility.AUTHED)
                    }
                }
                catch(e: NotImplementedError){
                    Log.e("NotImplemented", e.message, e)
                    threadCallback?.post {
                        updateProfileVisibility(ProfileVisibility.ERROR, getString(R.string.profile_not_implemented))//TODO: show better error
                    }

                }
                catch (e: Exception) {
                    if (Config.NEED_LOG)
                        Log.e("LoadProfileError", e.message, e)
                    threadCallback?.post {
                        updateProfileVisibility(ProfileVisibility.ERROR, getString(R.string.no_internet))//TODO: show better error
                    }
                }
            }
        }.also{it.start()}
    }
    private inner class OneMenuButton(val stringId: Int, val onItemClick: Runnable)
    private enum class ProfileVisibility{
        NOT_AUTHED,
        ERROR,
        LOADING,
        AUTHED
    }
}

