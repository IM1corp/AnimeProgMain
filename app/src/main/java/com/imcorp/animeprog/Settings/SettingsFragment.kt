package com.imcorp.animeprog.Settings

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.bold
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.DataBase
import com.imcorp.animeprog.DB.Settings
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.PreferenceProgress
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment.Companion.updateMenuSelectionItem
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var showBottomMenuPref: CheckBoxPreference
    private lateinit var showLeftMenuPref: CheckBoxPreference
    private lateinit var prefQuality:Preference
    private lateinit var themePref:Preference
    private val activity:MainActivity by lazy { requireActivity() as MainActivity }
    private fun setPreference(manager: PreferenceManager) {
        fun save(index: Int){
            activity.dataBase.settings.quality = OneVideo.VideoType.values()[index]
        }
        prefQuality = manager.findPreference(getString(R.string.pref_quality))!!
        prefQuality.setOnPreferenceClickListener{
            val checkedItem = activity.dataBase.settings.quality.ordinal
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(it.title)
                    .setIcon(R.drawable.ic_high_quality)
                    .setSingleChoiceItems(OneVideo.VideoType.STRING_VALUES, checkedItem){ dialog, index-> save(index);dialog.dismiss()}
                    .setNegativeButton(R.string.cancel){ dialog, _->dialog.dismiss()}
                    .show()
            true
        }

    }
    private fun setThemePref(manager: PreferenceManager){
        val stringArr:Array<String> = Settings.THEMES_STRING.map{requireContext().getString(it)}.toTypedArray()
        fun updateSum(index:Int?=null){
            themePref.summary = stringArr[index?.also{
                activity.dataBase.settings.theme=it
                activity.updateTheme(true)
            }?:activity.dataBase.settings.theme]
        }
        themePref = manager.findPreference<Preference>(getString(R.string.pref_theme_key)) as Preference
        themePref.setOnPreferenceClickListener{
            val theme = activity.dataBase.settings.theme
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(it.title)
                    .setIcon(R.drawable.ic_tv)
                    .setSingleChoiceItems(stringArr, theme){ dialog, index-> updateSum(index);dialog.dismiss()}
                    .setNegativeButton(R.string.cancel){ dialog, _->dialog.dismiss()}
                    .show()
            true
        }
        updateSum()
    }
    private fun backUpOnClick(manager: PreferenceManager) {
        manager.findPreference<Preference>(getString(R.string.pref_backup))!!.setOnPreferenceClickListener {
            BackUpDialog((requireActivity() as MyApp), R.style.CustomAlertDialog).show()
            false
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuSelectionItem(activity, R.id.settingsFragment)
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val manager = preferenceManager
        fun setCachePref(){
            val clearCachePref : PreferenceProgress = manager.findPreference(getString(R.string.pref_clear_cache))!!
            val cacheTitle = clearCachePref.title
            var cacheSize = activity.dataBase.cache.cacheSize
            var cacheFilesCount = activity.dataBase.cache.cacheFilesCount

            fun updateCacheTitle () {
                clearCachePref.title = SpannableStringBuilder(cacheTitle)
                        .append(" ( ")
                        .bold { append(cacheSize) }
                        .append(" )")
            }
            updateCacheTitle()
            clearCachePref.addOnBindListener {
                fun updateProgress(){
                    it.findViewById<ProgressBar>(R.id.progressBar).apply{
                        //progress = 0
                        max=activity.dataBase.settings.maxCacheFilesCount
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                            setProgress(cacheFilesCount, true)
                        else progress = cacheFilesCount
                    }
                    it.findViewById<TextView>(R.id.textViewP).text = "$cacheFilesCount / ${activity.dataBase.settings.maxCacheFilesCount}"

                }
                it.findViewById<View>(R.id.deleteButton).setOnClickListener {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                            .setIcon(R.drawable.ic_delete_forever)
                            .setTitle(R.string.verify_delete)
                            .setMessage(Config.getText(requireContext(), R.string.delete_cache, cacheSize))
                            .setPositiveButton(R.string.ok) { _, _->
                                activity.dataBase.cache.deleteCache(true)
                                cacheSize="0 KB"
                                cacheFilesCount=0
                                updateCacheTitle()
                                updateProgress()
                            }
                            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    builder.create().show()
                }
                updateProgress()
                clearCachePref.setOnPreferenceClickListener {
                    val view = LayoutInflater.from(requireContext()).inflate(R.layout.cache_save_input,null,false)
                    val text = view?.findViewById<EditText>(R.id.editTextNumber)!!
                    text.setText(activity.dataBase.settings.maxCacheFilesCount.toString(),TextView.BufferType.EDITABLE)
                    val builder = MaterialAlertDialogBuilder(requireContext())
                            .setIcon(R.drawable.ic_photo)
                            .setTitle(R.string.max_cache_files_count)
                            .setView(view)
                            .setPositiveButton(R.string.ok) { _, _->
                                activity.dataBase.settings.maxCacheFilesCount=text.text.toString().toInt()
                                updateProgress()
                            }
                            .setNegativeButton(R.string.cancel) { e, _->e.dismiss()}
                    builder.create().show()

                    false
                }
            }
        }
        manager.sharedPreferencesName = DataBase.SHARED_PREF_NAME
        addPreferencesFromResource(R.xml.preferences)

        setPreference(manager)
        setThemePref(manager)

        showBottomMenuPref = manager.findPreference<Preference>(getString(R.string.pref_bottom_panel_key)) as CheckBoxPreference
        showLeftMenuPref = manager.findPreference<Preference>(getString(R.string.pref_left_menu_key)) as CheckBoxPreference
        showBottomMenuPref.setOnPreferenceChangeListener { _, value ->
            if (false == value) showLeftMenuPref.isChecked = true
            activity.threadCallback.post { activity.updateShowMenu() }
            true
        }
        showLeftMenuPref.setOnPreferenceChangeListener { _, value: Any ->
            if (false == value) showBottomMenuPref.isChecked = true
            activity.threadCallback.post { activity.updateShowMenu() }
            true
        }
        setCachePref()
        //backUpOnClick(manager)
    }
}