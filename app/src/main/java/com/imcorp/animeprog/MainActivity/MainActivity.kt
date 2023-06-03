package com.imcorp.animeprog.MainActivity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.Objects.MySuggestionsAdapter
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.MyPopupMenu
import com.imcorp.animeprog.MainActivity.fragments.profile.ProfileFragment
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.fragments.downloads.DownloadsFragment
import com.imcorp.animeprog.MainActivity.fragments.favorites.FavoritesFragment
import com.imcorp.animeprog.MainActivity.fragments.home.HomeFragment
import com.imcorp.animeprog.MainActivity.search.SearchFragment
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData.FragmentOneAnimeData.UrlType
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.ActivityMainBinding
import java.util.*


class MainActivity : MyApp() {
    companion object {
        private val fragmentsSet = setOf(R.id.homeFragment, R.id.downloadsFragment, R.id.favoritesFragment, R.id.profileFragment)
        //private val fragmentsSetClasses = setOf(HomeFragment::class, DownloadsFragment::class, FavoritesFragment::class, ProfileFragment::class)
    }
    public lateinit var binding: ActivityMainBinding
    private lateinit var searchItem: MenuItem
    private lateinit var selectHostItem: MenuItem
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var searchView: SearchView
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.also{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { it.outlineProvider = null }
        })

        this.navHostFragment = supportFragmentManager.findFragmentById(R.id.mainFragment) as NavHostFragment


        /*navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            /*if(!dataBase.settings.topLeftMenuAvailable && fragmentsSetClasses.any {
                        it.qualifiedName == (destination as FragmentNavigator.Destination).className
                    })
                threadCallback.post{
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                }*/
        }*/
//        binding.navView.setNavigationItemSelectedListener {
//            true
//        }
        binding.navView.setNavigationItemSelectedListener {
            Log.i("ItemSelected", it.toString())
            onNavItemSelect(it)
            binding.drawerLayout.close()
            true
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item->
            onNavItemSelect(item)
            true
        }
        updateShowMenu()
        if (intent?.extras?.containsKey(Config.GO_TO_FRAGMENT_ID) == true) {
            val menuKey = intent!!.extras!!.getInt(Config.GO_TO_FRAGMENT_ID)
            this.onNavItemSelect(MyPopupMenu(this, binding.mainFragment).menu.add(0, menuKey, 0, ""))
        }
        binding.FAB.run {
            visibility = View.VISIBLE
            setOnClickListener {
                val currentHOST = dataBase.settings.mainHost
                HostChoicer(this@MainActivity,true)
                    .setEvents {
                        //onDismiss
                        if(currentHOST != dataBase.settings.mainHost) {
                            updateFAB()
                            (lastFragment as? HomeFragment)?.run{
//                                if (!isStateSaved)
                                    startLoading()
                            }
                            (lastFragment as? ProfileFragment)?.run {
                                this.initializeState(false)
                            }
                        }
                    }
                    .show()
            }
        }

        //nav_view.setupWithNavController(navHostFragment.navController)
        //bottomNavigationView.setupWithNavController(navHostFragment.navController)
        //bottomNavigationView.setupWithNavController(navHostFragment!!.navController,supportFragmentManager,)
        //this.showOneAnimeWindow("/anime/vnuk-mudreca-945");
    }


    fun updateFAB() = binding.FAB.setImageResource(Config.getIconByHost(dataBase.settings.mainHost))
    fun updateShowMenu(){
        val appBarConfigurationBuilder = AppBarConfiguration.Builder(fragmentsSet)
        if(dataBase.settings.topLeftMenuAvailable){
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            appBarConfigurationBuilder.setOpenableLayout(binding.drawerLayout)
        }
        else {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        binding.bottomNavigationView.visibility = if(!dataBase.settings.bottomMenuAvailable) View.GONE else View.VISIBLE

        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController, appBarConfigurationBuilder.build().also { appBarConfiguration = it })

    }
    private fun onNavItemSelect(item: MenuItem):Boolean{
        val args = Bundle()
        val controller = navHostFragment.findNavController()
        with(navHostFragment.childFragmentManager.fragments) {
            val navigateToId = when(item.itemId){
                R.id.favoritesFavItem,
                R.id.favoritesWatchLaterItem,
                R.id.favoritesWatchNowItem,
                R.id.favoritesWatchedItem,
                R.id.favoritesStopWatchedItem -> {
                    args.putInt(Config.GO_TO_FRAGMENT, item.itemId)
                    R.id.favoritesFragment
                }
                R.id.downloadsFragment,
                R.id.downloadsFragmentNow -> {
                    args.putInt(Config.GO_TO_FRAGMENT, item.itemId)
                    R.id.downloadsFragment
                }
                R.id.pageHistoryFragment,
                R.id.settingsFragment -> {
                    args.putInt(Config.GO_TO_FRAGMENT, item.itemId)
                    R.id.profileFragment
                }
                else->item.itemId
            }
            args.putBoolean(Config.IS_REFRESH, (this.size > 0 && when (navigateToId) {
                R.id.favoritesFragment -> FavoritesFragment::class.java
                R.id.profileFragment -> ProfileFragment::class.java
                R.id.downloadsFragment -> DownloadsFragment::class.java
                else -> HomeFragment::class.java
            } == this.last().javaClass))

            controller.navigate(navigateToId,args, NavOptions.Builder()
                .setEnterAnim(R.animator.fragment_open_enter)
                .setExitAnim(R.animator.fragment_open_exit)
                .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
                .setPopExitAnim(R.animator.nav_default_pop_exit_anim)
                .setRestoreState(true).build())
            if(fragmentsSet.contains(navigateToId))
                updateShowMenu()
            binding.bottomNavigationView.menu.findItem(navigateToId).isChecked=true
        }
        return true
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.START_ONE_ANIME_ACTIVITY_REQUEST_CODE) {
            if (data != null) {
                val extras = data.extras
                if (extras != null) {
                    //val anime: OneAnime? = extras.getParcelable(Config.FRAGMENT_ANIME_OBJ)

                    val g = extras.getInt(Config.URL_TYPE, -1)
                    if (g != -1) {
                        val type = UrlType.values()[g]
                        val path = extras.getString(Config.PATH)
                        when (type) {
                            UrlType.ANIME -> showOneAnimeWindow(path)
                            UrlType.STUDIO,
                            UrlType.GENRE,
                            UrlType.AUTHOR,
                            UrlType.PRODUCER -> {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
                                startActivity(browserIntent)

                            }
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
//    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount == 1) {
//            moveTaskToBack(false)
//        } else {
//            finish()
//        }
//    }
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navHostFragment.navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showOneAnimeWindow(path: String?) {
        val intent = Intent(this, OneAnimeActivity::class.java)
        intent.putExtra(Config.PATH, path)
        startActivityForResult(intent, Config.START_ONE_ANIME_ACTIVITY_REQUEST_CODE)
    }
    fun showOneAnimeWindow(anime: OneAnime?) {
        val intent = Intent(this, OneAnimeActivity::class.java)
        intent.putExtra(Config.FRAGMENT_ANIME_OBJ, anime)
        startActivityForResult(intent, Config.START_ONE_ANIME_ACTIVITY_REQUEST_CODE)
    }

    //region search
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.let{ query ->
                val f = lastFragment
                if(f is SearchFragment)
                    f.onFullSearchClick(query)
                //(lastFragment as? SearchFragment)?.searchAdapter?.lastSearch = query
                if(searchView.query.toString()!=query) searchView.setQuery(query, false)
                searchView.clearFocus()
//                this.currentFocus?.let { view ->
//                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
//                }

            }
        }
        else if (intent.action == "android.intent.action.VIEW"){
//            val args = HomeFragmentArgs.fromBundle(intent.extras!!);
            showOneAnimeWindow(intent.dataString)
//            navHostFragment.findNavController().navigate(R.id.homeFragment, intent.extras)
        }
    }
    private val lastFragment:Fragment get() = navHostFragment.childFragmentManager.fragments[0]
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        menuInflater.inflate(R.menu.top_menu, menu)
        selectHostItem=menu.findItem(R.id.select_host)
        searchItem = menu.findItem(R.id.action_search)
        searchView = (searchItem.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            layoutParams=ActionBar.LayoutParams(Gravity.END)
            queryHint = getString(R.string.query_hint)
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean {
                    (lastFragment as? SearchFragment)?.onLittleSearch(s)
                    return true
                }
            })
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View) {
                    try {
                        (lastFragment as? SearchFragment)?.run {
                            if (!isStateSaved) goBack()
                        }
                    } catch (e: IllegalStateException) {
                    }

                }

                override fun onViewAttachedToWindow(v: View) {
                    val f = lastFragment
                    if (f is SimpleFragment<*> && f.isSearching()) {
                        f.searchClick()
                        selectHostItem.isVisible = f is HomeFragment
                    }
                }
            })
            //SuggestionsAdapter
            suggestionsAdapter = MySuggestionsAdapter(context, this, searchManager.getSearchableInfo(componentName), WeakHashMap())
            (lastFragment as? SearchFragment)?.searchAdapter?.let{
                onActionViewExpanded()
                setQuery(it.lastSearch ?: "", false)
                searchItem.expandActionView()
                isIconified = false
                selectHostItem.isVisible = true
            }

        }
        updateSelectHostItemIcon()
        this.onResume()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.select_host -> {
            HostChoicer(this, false).setEvents(::updateSelectHostItemIcon).show()
//            SelectHostContextMenu(this, findViewById(R.id.select_host)).apply {
//                setOnDismissListener {updateSelectHostItemIcon()}
//            }.show()
            true
        }
        else->super.onOptionsItemSelected(item)
    }
    private fun updateSelectHostItemIcon() {
        selectHostItem.setIcon(Config.getIconByHost(dataBase.settings.searchHost))
    }
    //endregion

    fun closeSearchDialog() {
        searchView.onActionViewCollapsed()
        searchItem.collapseActionView()
        onSearchClose()
    }
    private fun onSearchClose(){
        selectHostItem.isVisible=false
    }

    fun loadFromLink(link: OneAnime.Link) {
        TODO("Not yet implemented")
    }
    public fun getCurrentMenuItemId(): Int = binding.navView.checkedItem?.itemId?:binding.bottomNavigationView.selectedItemId
}
