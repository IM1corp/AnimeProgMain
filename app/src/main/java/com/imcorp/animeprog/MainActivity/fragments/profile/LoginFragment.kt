package com.imcorp.animeprog.MainActivity.fragments.profile

import android.content.res.Configuration
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.hcaptcha.sdk.HCaptcha
import com.hcaptcha.sdk.HCaptchaConfig
import com.hcaptcha.sdk.HCaptchaSize
import com.hcaptcha.sdk.HCaptchaTheme
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.Notificator
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelLogin
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.YummyError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.CaptchaError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.InvalidPasswordError
import com.imcorp.animeprog.databinding.FragmentLoginBinding


class LoginFragment: SimpleFragment<FragmentLoginBinding>(R.layout.fragment_login, false, {FragmentLoginBinding.bind(it)}) {
    override val viewModel by navGraphViewModels<SaveStateModelLogin>(R.id.nav_graph)

    private var loadingThread: Thread? = null
    override fun onPause() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity.supportActionBar?.show()
        (activity as MainActivity).binding.bottomNavigationView.visibility = View.VISIBLE
        super.onPause()
    }

    override fun onStart() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity.supportActionBar?.hide()
        (activity as MainActivity).binding.bottomNavigationView.visibility = View.GONE
        super.onStart()
    }
    override fun initializeState(restore: Boolean) {
        scaleImage()
        viewBinding.registerTextView.movementMethod = LinkMovementMethod.getInstance()
        viewBinding.loginButton.setOnClickListener{authorize()}
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        scaleImage()
    }

    private fun scaleImage() {
//        val image = getDrawable(requireContext(), R.mipmap.bg1)!!.toBitmap()
//
//        val bitmap = BitmapDrawable()
//        viewBinding.bgImage
    }

    private fun authorize(recaptchaAns: String?=null){
        val login = viewBinding.loginTextField.text.toString()
        val password = viewBinding.passwordTextField.text.toString()
        loadingThread?.interrupt()
        loadingThread = Thread{
            try{
                val token = activity.request.auth(Config.HOST_YUMMY_ANIME, login, password, recaptchaAns)
                activity.dataBase.settings.yummyToken = token
                activity.runRunnableInUI {
                    findNavController().popBackStack()
                }
            }
            catch (e: Throwable){
                showError(e)
            }
        }.also{it.start()}
    }
    private fun showError(error: Throwable){
//        if(Config.NEED_LOG)
            Log.e("AuthError",error.message, error)
        (getActivity() as? MyApp)?.runRunnableInUI {
            when (error) {
                is NotImplementedError -> activity.showUndefinedError()
                is CaptchaError -> {
                    Notificator(activity).showCaptchaNeeded()

                    val hCaptcha: HCaptcha = HCaptcha.getClient(activity).apply{
                        addOnSuccessListener {
                            authorize(it.tokenResult)
                        }
                        addOnFailureListener {
                            showError(error)
                        }
                    }
                    hCaptcha.setup(HCaptchaConfig.builder()
                        .siteKey(Config.CaptchaPublicKey)
                        .size(HCaptchaSize.NORMAL)
                        .theme(if(activity.isThemeDark) HCaptchaTheme.DARK else HCaptchaTheme.LIGHT)
                        .build()
                    ).verifyWithHCaptcha()
                }
                is InvalidPasswordError -> {
                    viewBinding.passwordTextField.setText("")
                    Notificator(activity).showAuthError(this.viewBinding.root, error){authorize()}
                }
                is YummyError -> {
                    Notificator(activity).showAuthError(this.viewBinding.root, error){authorize()}
                }
                else-> activity.showUndefinedError(error.localizedMessage, error)
            }
        }
    }
//    private fun showRecaptchaDialog(client: RecaptchaClient){
//        lifecycleScope.launch {
//            client
//                .execute(RecaptchaAction.LOGIN)
//                .onSuccess { token ->
//                    authorize(token)
//                }
//                .onFailure { exception ->
//                    showError(exception)
//                }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingThread?.interrupt()
    }
}