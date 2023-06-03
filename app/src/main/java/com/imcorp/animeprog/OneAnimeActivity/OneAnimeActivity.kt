package com.imcorp.animeprog.OneAnimeActivity

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.local.Favorites.FavoritesType
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.Notificator
import com.imcorp.animeprog.Default.OnBackPressed
import com.imcorp.animeprog.DownloadManager.DownloadManager
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData.FragmentOneAnimeData
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData.FragmentOneAnimeData.UrlType
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.FragmentOneAnimeVideo
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.InvalidStatusException
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.ActivityOneAnimeBinding
import java.io.IOException
import java.io.InterruptedIOException
import java.util.*


class OneAnimeActivity : MyApp() {
    private var loadThread: Thread? = null

    //region vars
    public lateinit var binding: ActivityOneAnimeBinding
    private lateinit var path: String
    private var animeData: FragmentOneAnimeData? = null
    private var animeVideo: FragmentOneAnimeVideo? = null
    public var oneAnime: OneAnime? = null; private set;
    override fun onNewIntent(intent: Intent) {
        if(intent.extras?.containsKey("path") == true){
            this.loadMainActivity(intent);
            return;
        }
        when(intent.action){
            Config.NEXT_BUTTON_CLICKED -> {
                animeVideo?.nextVideoButtonClick()
            }
            Config.BACK_BUTTON_CLICKED -> {
                animeVideo?.backVideoButtonClick()
            }
            Config.PAUSE_BUTTON_CLICKED -> {
                if (intent.extras?.getBoolean(Config.IS_PAUSE_EVENT) == false)
                    animeVideo?.videoPlayer?.videoView?.start()
                else animeVideo?.videoPlayer?.videoView?.pause()
            }
        }
        super.onNewIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun initFragments() {
            val adapter = ViewPagerAdapter(supportFragmentManager)
            if (animeData == null) animeData = FragmentOneAnimeData()
            if (animeVideo == null) animeVideo = FragmentOneAnimeVideo()
            adapter.addFragment(animeData, getString(R.string.data))
            adapter.addFragment(animeVideo, getString(R.string.video))
            binding.tabLayout.setupWithViewPager(binding.viewPager.also { it.adapter = adapter })
        }
        super.onCreate(savedInstanceState)
        binding = ActivityOneAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)
        if (savedInstanceState != null) {
            animeData = supportFragmentManager.getFragment(savedInstanceState, Config.FRAGMENT_ANIME_DATA) as FragmentOneAnimeData?
            animeVideo = supportFragmentManager.getFragment(savedInstanceState, Config.FRAGMENT_ANIME_VIDEO) as FragmentOneAnimeVideo?
            path = savedInstanceState.getString(Config.PATH, "")
            savedInstanceState.getParcelable<OneAnime>(Config.FRAGMENT_ANIME_OBJ)?.let { oneAnime = it; }
        }
        else {
            val extras = intent.extras
            if (extras == null||
                    (extras.getString(Config.PATH, "").also { path = it }.isEmpty() &&
                     extras.getParcelable<OneAnime>(Config.FRAGMENT_ANIME_OBJ)?.also { oneAnime = it } == null)) {
                loadMainActivity(null)
                return
            }
        }
        oneAnime?.let{
            if(path.isEmpty())path = it.path
        }

        supportActionBar?.run{
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        initFragments()

        if (oneAnime?.isFullyLoaded != true) loadAnimeFromPath(null)
        else loadDataFromOneAnime(oneAnime!!)
        super.backCallbacks.add(OnBackPressed {
            loadMainActivity(null)
            true
        })
    }
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(Config.PATH, path)
        bundle.putParcelable(Config.FRAGMENT_ANIME_OBJ, oneAnime)
        with(supportFragmentManager){
            putFragment(bundle, Config.FRAGMENT_ANIME_DATA, animeData!!)
            putFragment(bundle, Config.FRAGMENT_ANIME_VIDEO, animeVideo!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //val menuInflater: MenuInflater = SupportMenuInflater(ContextThemeWrapper(applicationContext, R.style.popupMenuStyle))

        menuInflater.inflate(R.menu.one_anime_menu, menu)

        //update icon if in favorites
        Thread {
            val isF = dataBase.favorites.isFavorite(path, (oneAnime?.HOST ?: getHost()).toInt()) != null
            threadCallback.post {
                val item = menu.findItem(R.id.actionFavorites)
                item.setIcon(if (isF) R.drawable.ic_star else R.drawable.ic_start_not_selected)
            }
        }.start()
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        fun pleaseWaitUntilLoaded() = Toast.makeText(this, R.string.wait_untill_anime_loaded, Toast.LENGTH_SHORT).show()
        if(oneAnime==null) pleaseWaitUntilLoaded()
        else when(item.itemId){
            R.id.actionDownloads -> DownloadManager(this, oneAnime).showWindow()
            R.id.actionFavorites -> Thread { showFavoritesWindow(item) }.start()
            R.id.actionCopyLink -> copyText(oneAnime!!.animeURI)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFavoritesWindow(item: MenuItem) {
        val type = dataBase.favorites.isFavorite(oneAnime!!.path, oneAnime!!.HOST.toInt())
        val checked = if (type == null) null else FavoritesType.orderToShow.indexOf(type.type)
        val builder = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.favorites)
                .setSingleChoiceItems(R.array.favorites_items, checked ?: -1) { dialog, which ->
                    dataBase.favorites.addToFavorites(oneAnime, FavoritesType.orderToShow[which])
                    item.setIcon(R.drawable.ic_star)
                    notificator.showAddedAnimeToFavorites(Notificator.STATE_ADDED) { onOptionsItemSelected(item) }
                    dialog.cancel()
                }
                .setPositiveButton(R.string.cancel) { dialog, _ ->  dialog.cancel() }
        if (type != null) builder.setNegativeButton(R.string.delete) { dialog: DialogInterface, _->
            dataBase.favorites.deleteFromFavorites(path, oneAnime!!.HOST.toInt())
            item.setIcon(R.drawable.ic_start_not_selected)
            notificator.showAddedAnimeToFavorites(Notificator.STATE_DELETED) { onOptionsItemSelected(item) }
            dialog.dismiss()
        }
        threadCallback.post { builder.show() }
    }

    //endregion
    private fun loadDataFromOneAnime(anime: OneAnime) {
        dataBase.history.addToHistory(anime)
        oneAnime = anime
        supportActionBar?.title = anime.title

        animeData!!.setData(anime)
        animeVideo!!.setData(anime)
    }
    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            findViewById<View>(R.id.errorTextView).visibility = View.GONE
            findViewById<View>(R.id.retryErrorButton).visibility = View.GONE
            if (loading) {
                binding.loadingProgressBar.visibility = View.VISIBLE
            } else {
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }
    fun loadAnimeFromPath(ignored: View?) {
        setLoading(true)
        loadThread = Thread {
            try {
                val an: OneAnime = request.loadAnimeFromPath(
                        path,
                        (this.oneAnime?.HOST ?: Config.getHostByUrl(path)).toInt()
                )
                threadCallback.post {
                    loadDataFromOneAnime(an)
                    setLoading(false)
                }

                an.comments.loadComments(an,this@OneAnimeActivity, 0)
                threadCallback.post {

                    animeVideo!!.setCommentsData()
                }
            } catch (e: InvalidHtmlFormatException) {
                showErrorWithText(e.message)
            } catch (ignored1: InterruptedIOException) {
                showErrorWithText(getString(R.string.undefined_error))
            } catch (e: InvalidStatusException){
                if(e.status==404)
                    showErrorWithText(getString(R.string.no_page_found))
                else showErrorWithText(getString(R.string.undefined_error))
            } catch (e: IOException) {
                showErrorWithText(getString(R.string.no_internet))
            }
        }
        loadThread!!.start()
    }

    //region service
    override fun onSupportNavigateUp(): Boolean {
        loadMainActivity(null)
        return true
    }
    private fun showErrorWithText(message: String?) = runOnUiThread {
        setLoading(false)
        binding.errorTextView.run{
            text = message
            visibility = View.VISIBLE
        }
        binding.retryErrorButton.visibility = View.VISIBLE;
    }
    private fun loadMainActivity(intent: Intent?) {
        setResult(Activity.RESULT_OK, (intent
                ?: Intent()).putExtra(Config.FRAGMENT_ANIME_OBJ, oneAnime))
        finish()
        //finishAffinity();
    }
    fun onPathClicked(type: UrlType, path: String?) {
        loadMainActivity(Intent().apply {
            putExtra(Config.URL_TYPE, type.ordinal)
            putExtra(Config.PATH, path)
        })
    }
    //endregion
    fun loadVideoFromAnotherSites() {
        //TODO: delete
    }
    private fun getHost()=if(path.isNotBlank())path.run{
        return when {
            startsWith(Config.ANIMEGO_URL) -> Config.HOST_ANIMEGO_ORG
            startsWith(Config.ANIMEJOY_URL) -> Config.HOST_ANIME_JOY
            startsWith(Config.GOGOANIME_URL) -> Config.HOST_GOGO_ANIME
            //startsWith(Config.YUMMY_ANIME_URL) -> Config.HOST_YUMMY_ANIME
            else -> Config.HOST_YUMMY_ANIME
        }
    }else Config.HOST_YUMMY_ANIME
    override fun onDestroy() {
        loadThread?.interrupt()
        super.onDestroy()
    }
}