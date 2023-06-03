package com.imcorp.animeprog.MainActivity.search

import android.util.Log
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.Requests.Http.InvalidStatusException
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import org.json.JSONException
import java.io.IOException
import java.io.InterruptedIOException

public abstract class SimpleSearchAdapter(val fragment: SearchFragment, private val events: OnSuccess) {
    companion object{
        val emptyRegex:Regex = " ".toRegex()
    }
    val activity:MainActivity get() = fragment.activity as MainActivity;

    private var searchThreadLittle:Thread? = null;
    var searchThreadBig: Thread? = null
    public var lastSearch: String? = null
    private fun postDelayedLittle () {
        searchThreadLittle?.run{
            if(isAlive) interrupt()
        }
        searchThreadLittle = Thread {
            try {
                val data = getLittleData(lastSearch!!);
                activity.threadCallback.post {
                    events.onSuccessLittle(data)
                }
            } catch (ignored: InterruptedIOException) { }
            catch (e: JSONException) {
                activity.showInvalidJsonError()
            }
            catch (e: InvalidStatusException) {
                if (Config.NEED_LOG) Log.e(Config.LOG_TAG, "Invalid status : " + e.status)
                activity.showInvalidStatusException()
            }
            catch (e: IOException) {
                activity.showNoInternetException(e)
            }
        }.apply { start(); }
    }
    fun g(a: List<Int> = listOf(10, 20)){}
    fun onLittleSearch(s: String, offset: Int = 0) {
        if (s.length >= 2 &&
                (lastSearch == null ||
                        lastSearch!!.replace(emptyRegex, "") !=
                        s.replace(emptyRegex, ""))) {
            lastSearch = s
            activity.threadCallback.removeCallbacks(this::postDelayedLittle)
            activity.threadCallback.postDelayed(this::postDelayedLittle, Config.SEARCH_WAIT_TIME_MS)
        }

    }
    fun onBigSearch(s: String, offset: Int = 0){
        if(s.length >= 3) {
            fragment.setLoading(true)
            this.searchThreadBig?.interrupt()
            this.searchThreadBig = Thread {
                try {
                    val data = getBigSearchData(s)
                    activity.threadCallback.post {
                        events.onSuccessBig(data)
                    }
                } catch (ignored: InterruptedIOException) {
                } catch (e: JSONException) {
                    activity.showInvalidJsonError()
                } catch (e: InvalidStatusException) {
                    if (Config.NEED_LOG) Log.e(Config.LOG_TAG, "Invalid status : " + e.status)
                    activity.showInvalidStatusException()
                } catch (e: IOException) {
                    activity.showNoInternetException()
                }
            }.also{it.start()}
        }
    }
    abstract fun getLittleData(q: String): ArrayList<OneAnime.OneAnimeWithId>
    abstract fun getBigSearchData(q: String): ArrayList<OneAnime.OneAnimeWithId>
    interface OnSuccess{
        public fun onSuccessLittle(data: ArrayList<OneAnime.OneAnimeWithId>, count: Int = 0);
        public fun onSuccessBig(data: ArrayList<OneAnime.OneAnimeWithId>, count: Int = 0);
    }
}