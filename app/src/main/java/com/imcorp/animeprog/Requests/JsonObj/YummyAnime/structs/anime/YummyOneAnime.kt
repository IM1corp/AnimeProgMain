package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import android.content.res.Resources
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.YummyIAnimeJson

public class YummyOneAnime(
        @SerializableField("other_titles") public var otherTitles: ArrayList<String> = arrayListOf(),
        @SerializableField("rating") public var rating: YummyRating=YummyRating(),
        @SerializableField("remote_ids") public var remoteIds: YummyRemoteIds=YummyRemoteIds(),
        @SerializableField("status") public var status: Int = 0,
        @SerializableField("year") public var year: Int = 0,
        @SerializableField("creators") public var creators: ArrayList<YummyCreator> = arrayListOf(),
        @SerializableField("studios") public var studios: ArrayList<YummyStudio> = arrayListOf(),
        @SerializableField("videos") public var videos: ArrayList<YummyVideo> = arrayListOf(),
        @SerializableField("genres") public var genres: ArrayList<YummyGenre> = arrayListOf(),
        @SerializableField("season") public var seasonInt: Int = 0,
        @SerializableField("min_age") public var minAge: YummyMinAge = YummyMinAge(),
        @SerializableField("views") public var views: Long = 0,
        @SerializableField("original") public var original: String? = "",
        @SerializableField("user") public var userData: YummyAnimeUserData? = null,
        @SerializableField("viewing_order") public var viewingOrder: List<YummyViewingOrder> = listOf(),
        @SerializableField("blocked_in") public var blockedIn: Array<String> = arrayOf(),
        @SerializableField("translates") public var tranlates: List<YummyTranslate> = listOf()
): YummyIAnimeJson() {
        public fun seasonString(resources: Resources): String? = when(this.seasonInt){
                1-> resources.getString(R.string.season_1)
                2-> resources.getString(R.string.season_2)
                3-> resources.getString(R.string.season_3)
                4-> resources.getString(R.string.season_4)
                else->null
        }
        public fun statusString(resources: Resources): String = when(this.status){
                1->resources.getString(R.string.status_1)
                2->resources.getString(R.string.status_2)
                else->resources.getString(R.string.status_0)
        }
        public fun blockedInCountries(resources: Resources): Array<String> = Array(this.blockedIn.size){
                "${blockedIn[it]} ("+when(blockedIn[it]){
                        "AU"-> "Австралия"
                "AT"-> "Австрия"
                "AZ"-> "Азербайджан"
                "AX"-> "Аландские острова"
                "AL"-> "Албания"
                "DZ"-> "Алжир"
                "VI"-> "Виргинские Острова (США)"
                "AS"-> "Американское Самоа"
                "AI"-> "Ангилья"
                "AO"-> "Ангола"
                "AD"-> "Андорра"
                "AQ"-> "Антарктида"
                "AG"-> "Антигуа и Барбуда"
                "AR"-> "Аргентина"
                "AM"-> "Армения"
                "AW"-> "Аруба"
                "AF"-> "Афганистан"
                "BS"-> "Багамы"
                "BD"-> "Бангладеш"
                "BB"-> "Барбадос"
                "BH"-> "Бахрейн"
                "BZ"-> "Белиз"
                "BY"-> "Белоруссия"
                "BE"-> "Бельгия"
                "BJ"-> "Бенин"
                "BM"-> "Бермуды"
                "BG"-> "Болгария"
                "BO"-> "Боливия"
                "BQ"-> "Бонэйр, Синт-Эстатиус и Саба"
                "BA"-> "Босния и Герцеговина"
                "BW"-> "Ботсвана"
                "BR"-> "Бразилия"
                "IO"-> "Британская территория в Индийском океане"
                "VG"-> "Британские Виргинские острова"
                "BN"-> "Бруней"
                "BF"-> "Буркина-Фасо"
                "BI"-> "Бурунди"
                "BT"-> "Бутан"
                "VU"-> "Вануату"
                "VA"-> "Ватикан"
                "GB"-> "Великобритания"
                "HU"-> "Венгрия"
                "VE"-> "Венесуэла"
                "UM"-> "Внешние малые острова (США)"
                "TL"-> "Восточный Тимор"
                "VN"-> "Вьетнам"
                "GA"-> "Габон"
                "HT"-> "Гаити"
                "GY"-> "Гайана"
                "GM"-> "Гамбия"
                "GH"-> "Гана"
                "GP"-> "Гваделупа"
                "GT"-> "Гватемала"
                "GF"-> "Гвиана"
                "GN"-> "Гвинея"
                "GW"-> "Гвинея-Бисау"
                "DE"-> "Германия"
                "GG"-> "Гернси"
                "GI"-> "Гибралтар"
                "HN"-> "Гондурас"
                "HK"-> "Гонконг"
                "GD"-> "Гренада"
                "GL"-> "Гренландия"
                "GR"-> "Греция"
                "GE"-> "Грузия"
                "GU"-> "Гуам"
                "DK"-> "Дания"
                "JE"-> "Джерси"
                "DJ"-> "Джибути"
                "DM"-> "Доминика"
                "DO"-> "Доминиканская Республика"
                "CD"-> "Демократическая Республика Конго"
                "EU"-> "Европейский союз"
                "EG"-> "Египет"
                "ZM"-> "Замбия"
                "EH"-> "САДР"
                "ZW"-> "Зимбабве"
                "IL"-> "Израиль"
                "IN"-> "Индия"
                "ID"-> "Индонезия"
                "JO"-> "Иордания"
                "IQ"-> "Ирак"
                "IR"-> "Иран"
                "IE"-> "Ирландия"
                "IS"-> "Исландия"
                "ES"-> "Испания"
                "IT"-> "Италия"
                "YE"-> "Йемен"
                "CV"-> "Кабо-Верде"
                "KZ"-> "Казахстан"
                "KY"-> "Острова Кайман"
                "KH"-> "Камбоджа"
                "CM"-> "Камерун"
                "CA"-> "Канада"
                "QA"-> "Катар"
                "KE"-> "Кения"
                "CY"-> "Кипр"
                "KG"-> "Кыргыстан"
                "KI"-> "Кирибати"
                "TW"-> "Китайская Республика"
                "KP"-> "КНДР"
                "CN"-> "КНР"
                "CC"-> "Кокосовые острова"
                "CO"-> "Колумбия"
                "KM"-> "Коморы"
                "CR"-> "Коста-Рика"
                "CI"-> "Кот-д’Ивуар"
                "CU"-> "Куба"
                "KW"-> "Кувейт"
                "CW"-> "Кюрасао"
                "LA"-> "Лаос"
                "LV"-> "Латвия"
                "LS"-> "Лесото"
                "LR"-> "Либерия"
                "LB"-> "Ливан"
                "LY"-> "Ливия"
                "LT"-> "Литва"
                "LI"-> "Лихтенштейн"
                "LU"-> "Люксембург"
                "MU"-> "Маврикий"
                "MR"-> "Мавритания"
                "MG"-> "Мадагаскар"
                "YT"-> "Майотта"
                "MO"-> "Макао"
                "MK"-> "Македония"
                "MW"-> "Малави"
                "MY"-> "Малайзия"
                "ML"-> "Мали"
                "MV"-> "Мальдивы"
                "MT"-> "Мальта"
                "MA"-> "Марокко"
                "MQ"-> "Мартиника"
                "MH"-> "Маршалловы Острова"
                "MX"-> "Мексика"
                "FM"-> "Микронезия"
                "MZ"-> "Мозамбик"
                "MD"-> "Молдавия"
                "MC"-> "Монако"
                "MN"-> "Монголия"
                "MS"-> "Монтсеррат"
                "MM"-> "Мьянма"
                "NA"-> "Намибия"
                "NR"-> "Науру"
                "NP"-> "Непал"
                "NE"-> "Нигер"
                "NG"-> "Нигерия"
                "NL"-> "Нидерланды"
                "NI"-> "Никарагуа"
                "NU"-> "Ниуэ"
                "NZ"-> "Новая Зеландия"
                "NC"-> "Новая Каледония"
                "NO"-> "Норвегия"
                "AE"-> "ОАЭ"
                "OM"-> "Оман"
                "BV"-> "Остров Буве"
                "IM"-> "Остров Мэн"
                "CK"-> "Острова Кука"
                "NF"-> "Остров Норфолк"
                "CX"-> "Остров Рождества"
                "PN"-> "Острова Питкэрн"
                "SH"-> "Острова Святой Елены, Вознесения и Тристан-да-Кунья"
                "PK"-> "Пакистан"
                "PW"-> "Палау"
                "PS"-> "Государство Палестина"
                "PA"-> "Панама"
                "PG"-> "Папуа — Новая Гвинея"
                "PY"-> "Парагвай"
                "PE"-> "Перу"
                "PL"-> "Польша"
                "PT"-> "Португалия"
                "PR"-> "Пуэрто-Рико"
                "CG"-> "Республика Конго"
                "KR"-> "Республика Корея"
                "RE"-> "Реюньон"
                "RU"-> "Россия"
                "RW"-> "Руанда"
                "RO"-> "Румыния"
                "SV"-> "Сальвадор"
                "WS"-> "Самоа"
                "SM"-> "Сан-Марино"
                "ST"-> "Сан-Томе и Принсипи"
                "SA"-> "Саудовская Аравия"
                "SZ"-> "Свазиленд"
                "MP"-> "Северные Марианские острова"
                "SC"-> "Сейшельские Острова"
                "BL"-> "Сен-Бартелеми"
                "MF"-> "Сен-Мартен"
                "PM"-> "Сен-Пьер и Микелон"
                "SN"-> "Сенегал"
                "VC"-> "Сент-Винсент и Гренадины"
                "KN"-> "Сент-Китс и Невис"
                "LC"-> "Сент-Люсия"
                "RS"-> "Сербия"
                "SG"-> "Сингапур"
                "SX"-> "Синт-Мартен"
                "SY"-> "Сирия"
                "SK"-> "Словакия"
                "SI"-> "Словения"
                "SB"-> "Соломоновы Острова"
                "SO"-> "Сомали"
                "SD"-> "Судан"
                "SU"-> "СССРсентября1992 года"
                "SR"-> "Суринам"
                "US"-> "США"
                "SL"-> "Сьерра-Леоне"
                "TJ"-> "Таджикистан"
                "TH"-> "Таиланд"
                "TZ"-> "Танзания"
                "TC"-> "Тёркс и Кайкос"
                "TG"-> "Того"
                "TK"-> "Токелау"
                "TO"-> "Тонга"
                "TT"-> "Тринидад и Тобаго"
                "TV"-> "Тувалу"
                "TN"-> "Тунис"
                "TM"-> "Туркменистан"
                "TR"-> "Турция"
                "UG"-> "Уганда"
                "UZ"-> "Узбекистан"
                "UA"-> "Украина"
                "WF"-> "Уоллис и Футуна"
                "UY"-> "Уругвай"
                "FO"-> "Фареры"
                "FJ"-> "Фиджи"
                "PH"-> "Филиппины"
                "FI"-> "Финляндия"
                "FK"-> "Фолклендские острова"
                "FR"-> "Франция"
                "PF"-> "Французская Полинезия"
                "TF"-> "Французские Южные и Антарктические Территории"
                "HM"-> "Херд и Макдональд"
                "HR"-> "Хорватия"
                "CF"-> "ЦАР"
                "TD"-> "Чад"
                "ME"-> "Черногория"
                "CZ"-> "Чехия"
                "CL"-> "Чили"
                "CH"-> "Швейцария"
                "SE"-> "Швеция"
                "SJ"-> "Шпицберген и Ян-Майен"
                "LK"-> "Шри-Ланка"
                "EC"-> "Эквадор"
                "GQ"-> "Экваториальная Гвинея"
                "ER"-> "Эритрея"
                "EE"-> "Эстония"
                "ET"-> "Эфиопия"
                "ZA"-> "ЮАР"
                "GS"-> "Южная Георгия и Южные Сандвичевы острова"
                "SS"-> "Южный Судан"
                "JM"-> "Ямайка"
                "JP"-> "Япония"
                        else->"..."
                }+")"
        }

        override fun toOneAnime(resources: Resources): OneAnime = super.toOneAnime(resources).also{anime->
                anime.year = this.year
                anime.status = this.statusString(resources)
                anime.watches = this.views
                anime.attrs.synonyms = this.otherTitles
                anime.attrs.genres = ArrayList(this.genres.map{OneAnime.Link(it.title, it.url)})
                if(this.rating.average != 0.0)
                        anime.attrs.rating = String.format("%.2f",this.rating.average)
                anime.attrs.originalSource = this.original
                anime.attrs.producers = ArrayList(this.creators.map { OneAnime.Link(it.title, it.url)})
                anime.attrs.studios = ArrayList(this.studios.map { OneAnime.Link(it.title, it.url)})
                anime.viewingOrder = ArrayList(this.viewingOrder.map {it.toOneAnime(resources)})
                anime.videos = this.buildVideos()
        }

        private fun buildVideos(): ArrayList<OneVideoEP> {
                val keys = this.videos.map{it.epNumber}.toHashSet().toTypedArray()
                val ans = HashMap<String, OneVideoEP>().apply {
                        for (key in keys) this[key] = OneVideoEP(true).apply{this.num = key;}
                }

                for(i in this.videos){
                        ans[i.epNumber]!!.initVideos(0).add(i.toOneVideo())
                }

                return ArrayList(ans.values)
        }
}

