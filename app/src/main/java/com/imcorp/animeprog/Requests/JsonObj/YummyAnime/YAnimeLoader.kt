package com.imcorp.animeprog.Requests.JsonObj.YummyAnime

import android.content.Context
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.find
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.OneAnime.VideoResponseType
import com.imcorp.animeprog.Requests.JsonObj.SimpleAnimeLoader
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import kotlin.collections.ArrayList


object YAnimeLoader : SimpleAnimeLoader {
    override fun fromHtml(html: Document, path: String, context: Context): OneAnime = OneAnime(Config.HOST_YUMMY_ANIME).apply{
        val dataInfo = html.selectFirst(".content-main-info") ?: throw InvalidHtmlFormatException("Invalid HTML - not content info")
        fun loadAttrs() = with(attrs){
            fun loadEpisodesCount(): Int = try {
                val li = getMainInfoElementFromKeyString(dataInfo, "Серии")
                li.ownText().trim().toInt()
            } catch (_: InvalidHtmlFormatException) { 0 } catch (_: NumberFormatException) { 0 }
            fun loadSynonyms(): ArrayList<String> = ArrayList(
                    html.select(".alt-names-list li")
                            .map { it.ownText() }
            )
            fun loadStudio(): ArrayList<OneAnime.Link> =try{
                val list = getMainInfoElementFromKeyString(dataInfo, "Студия")
                        ?.select("a")
                        ?.map{ OneAnime.Link(it) }
                ArrayList(list)
            }catch (ex: InvalidHtmlFormatException){
                arrayListOf()
//                OneAnime.Link.noHref("")
            }
            fun loadRating(): String? = try{getMainInfoElementFromKeyString(dataInfo, "Возраст").ownText() }catch (e: InvalidHtmlFormatException){null}
            fun loadOriginalSource():String? = try{getMainInfoElementFromKeyString(dataInfo, "Первоисточник").ownText()}catch (e: InvalidHtmlFormatException){null}
            fun loadSerialType():String? = try{getMainInfoElementFromKeyString(dataInfo, "Тип").text()}catch (e: InvalidHtmlFormatException){null}
            epCount = loadEpisodesCount()
            synonyms = loadSynonyms()
            studios = loadStudio()
            rating = loadRating()
            genres = loadGenres(dataInfo)
            originalSource = loadOriginalSource()
            serialType = loadSerialType()
        }
        fun loadAnimeData(){
            fun loadWatches(): Long =try { getMainInfoElementFromKeyString(dataInfo, "Просмотров")
                    .ownText()
                    .replace(" ", "")
                    .toLong()
            } catch (e: NumberFormatException) { 0 }
            fun loadYear(): Int = try {
                getMainInfoElementFromKeyString(dataInfo, "Год")
                        .ownText()
                        .trim()
                        .toInt()
            } catch (e: NumberFormatException) { 0 }
            fun loadType(): String? = try { getMainInfoElementFromKeyString(dataInfo, "Тип").ownText().trim() } catch (ex: InvalidHtmlFormatException) { null }
            fun loadStatus(): String? = try {
                val li = getMainInfoElementFromKeyString(dataInfo, "Статус")
                if (li.children().size != 2) null else li.child(1).ownText()
            } catch (ex: InvalidHtmlFormatException) { "Anime" }
            watches = loadWatches()
            year = loadYear()
            animeType = loadType()
            status = loadStatus()
        }
        title = parseTitleFromHtml(html)
        description = parseDescriptionFromHtml(html)
        viewingOrder = parseViewingOrderFromHtml(html)
        setPath(path)
        cover = getImageFromHtml(html).also { setBigCover(it) }

        loadAnimeData()
        loadAttrs()
        isFullyLoaded = true
        this.videos = loadVideosNoDubSelect(html, this);

        val pageId = html.find("#page_id").attr("value").toInt();
        comments.defaultUrl = "/comments/anime/$pageId"


        //item.videos = loadVideos(html);
    }
    fun loadGenres(dataInfo: Element): ArrayList<OneAnime.Link> = try {
        ArrayList(
                getMainInfoElementFromKeyString(dataInfo, "Жанр")
                        .select("a")
                        .map { OneAnime.Link(it) }
        )
    } catch (e: InvalidHtmlFormatException){ArrayList()}
    //region data
    private fun getMainInfoElementFromKeyString(document: Element, key: String): Element {
        val data = document.select("span:containsOwn($key)")
        if (data.size == 0) throw InvalidHtmlFormatException("HTML is invalid - no count of watches")
        return data.first().parent()
    }
    private fun getImageFromHtml(j: Document): String {
        val documents = j.select(".poster-block img")
        if (documents.size != 1) throw InvalidHtmlFormatException("HTML is invalid - no cover")
        var src = documents.first().attr("src")
        if (!src.startsWith("http"))
            src = Config.YUMMY_ANIME_URL + (if (!src.startsWith("/"))"/" else "") + src

        return src
    }
    private fun parseDescriptionFromHtml(j: Document): String {
        val desc = j.select("#content-desc-text")
        if (desc.size == 0) throw InvalidHtmlFormatException("HTML is invalid - no description")
        val el = desc.first()
        val ch = el.children()
        return if (ch.size != 0) {
            val builder = StringBuilder()
            for (i in ch) {
                builder.append(i.text())
                builder.append("\n")
            }
            builder.toString()
        } else el.text()
    }
    private fun parseTitleFromHtml(j: Document): String {
        val title = j.select("h1")
        if (title.size == 0) throw InvalidHtmlFormatException("HTML is invalid - no title")
        return title[0].text()
    }
    private fun parseViewingOrderFromHtml(j: Document): ArrayList<OneAnime>? {
        val elements = j.select(".view-list li")
        if (elements.size == 0) return null

        val viewingOrder = ArrayList<OneAnime>()
        for (i in elements) {
            var add=true
            val anime = OneAnime(Config.HOST_YUMMY_ANIME).apply {
                val text: String
                if (i.children().size == 0) {
                    val ss = i.ownText().split("-", limit = 2).toTypedArray()
                    if (ss.size != 2) {
                        title = ss[0].trim()
                        description = ""
                        year = Calendar.getInstance().time.year
                        add=false
                        return@apply
                    }
                    title = ss[0].trim()
                    text = "- " + ss[1].trim()
                } else {
                    val a = i.child(0)
                    path = a.attr("href")
                    title = a.text()
                    text = i.ownText().trim()
                }
                val yearSTR = text.split(", ").last()
                year = try {
                    yearSTR.toInt()
                } catch (e: NumberFormatException) {
                    Calendar.getInstance().time.year
                }
                if (!text.startsWith("- ")) throw InvalidHtmlFormatException("invalid html - viewing order name invalid")
                description = text.substring(2, text.length - yearSTR.length - 2).trim() // убираем года с конца и убираем ', '
            }
            if(add) viewingOrder.add(anime)
        }
        return viewingOrder
    }

    //endregion
        /*private static VideosResponse loadVideos(Element element){
            return loadVideosNoDubSelect(element);

            VideosResponse answer = new VideosResponse();
            Elements elements = element.select("#dub-select option");
            if(elements.size()==0){
                VideosResponse v = loadVideosNoDubSelect(element);
                if(v!=null)return v;
            }
            else {
                for (Element option : elements) {
                    int value = Integer.parseInt(option.attr("value"));
                    String title = option.text();

                    Elements players = element.select(String.format(Locale.US, "#player-select div[data-dub='%d'] option", value));
                    final ArrayList<OneVideo> videos = new ArrayList<>(players.size());
                    for (int i = 0; i < players.size(); i++) {
                        Element single_element = players.get(i);

                        OneVideo video = new OneVideo();
                        video.loadOneVideoPlayerFromUrl(single_element.text().toLowerCase());
                        if (video.player == null) continue;//Неизвестный плеер

                        if (videos.size() > 0) {
                            video.voiceAuthors = videos.get(0).voiceAuthors;
                        } else {
                            video.voiceAuthors = loadVoiceAuthorsFromStudioTitle(element, title);
                        }
                        video.loadEpisodesFromHtml(element);
                        videos.add(video);
                    }
                    answer.videos.add(new OneVideoKeyValuePair(title, videos));
                }
            }
            return answer;
        }*/
    private fun loadVideosNoDubSelect(element: Element, anime: OneAnime): ArrayList<OneVideoEP> {
        class OneVideoBlock(private val videoBlockEl: Element){
            private val descEl: Element = videoBlockEl.selectFirst(".video-block-description")?: throw InvalidHtmlFormatException("No video-block description element found")
            val dataEpisodes:ArrayList<Element> = videoBlockEl.select(".episodes-container .video-button")
            private val dubbing: String = getDubbingFromEl()
            private fun getDubbingFromEl(): String {//Like "Озвучка AniLibria. Плеер Kodik  (5 эпизодов)"
                var desc:String = descEl.text()
                if (desc.contains("(")) desc = desc.substring(0, desc.indexOf("("))
                val dataTitles:Array<String> = desc.split('.').toTypedArray()
                return dataTitles[(if (dataTitles.size == 1 ||
                        dataTitles[0].toLowerCase(Locale.ROOT).contains("озву") ||
                        dataTitles[1].toLowerCase(Locale.ROOT).contains("плеер")
                ) 0 else 1)].trim()
            }
            fun getOneVideoFromDataEp(index: Int) : OneVideo {
                val episode = dataEpisodes[index]
                return OneVideo(episode.text()).apply {
                    voiceStudio = dubbing
                    urlFrame = episode.attr("data-href")
                    loadOneVideoPlayerFromUrl()
                }
            }
        }
        val els: List<OneVideoBlock> = element.select(".video-block").map { OneVideoBlock(it)}
                .also{if (it.isEmpty()) {
                    anime.responseType = getResponseErrorTypeFromHtml(element)
                    return ArrayList()
                }}
        val epCount = els.maxOf { it.dataEpisodes.size }
        return ArrayList<OneVideoEP>(epCount).apply{
            for(episodeIndex in 0 until epCount) {
                val ep = OneVideoEP()
                val videos = ep.initVideos(els.size).apply {
                    for (block in els)
                        if (block.dataEpisodes.size > episodeIndex)
                            add(block.getOneVideoFromDataEp(episodeIndex))
                }
                ep.num = videos.firstOrNull()?.num ?: (episodeIndex + 1).toString()
                this.add(ep)
            }
        }
    }
    private fun getResponseErrorTypeFromHtml(element: Element): VideoResponseType {
        val error = element.select(".status-bg")
        if (error.size == 0) return VideoResponseType.UNDEFINED
        val text = error[0].text()
        if (text.contains("недоступно") || text.contains("лицензировано")) {
            return VideoResponseType.VIDEO_NOT_ABLE_IN_YOUR_COUNTRY
        } else if (text.contains("удалено")) {
            return VideoResponseType.VIDEO_DELETED
        }
        return VideoResponseType.UNDEFINED
    }

    private fun loadVoiceAuthorsFromStudioTitle(element: Element, studio_title: String): Array<String?>? {
        val _s = studio_title.split(" ".toRegex()).toTypedArray()
        val studio_name = _s[_s.size - 1]
        var voiceAuthors: Array<String?>? = null
        val voiceStudioAuthors = element.select(String.format(".studio-name:containsOwn(%s)", studio_name))
        if (voiceStudioAuthors.size != 0) {
            val li = voiceStudioAuthors[0].parent().select("ul li")
            voiceAuthors = arrayOfNulls(li.size)
            for (j in li.indices) {
                voiceAuthors[j] = li[j].text()
            }
        }
        return voiceAuthors
    }
}