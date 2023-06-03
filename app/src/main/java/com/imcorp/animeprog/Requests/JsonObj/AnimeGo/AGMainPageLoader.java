package com.imcorp.animeprog.Requests.JsonObj.AnimeGo;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.HtmlUtils;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.mainPage.Container;
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage;
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.imcorp.animeprog.Default.ArrayUtilsKt.any;

public class AGMainPageLoader {
    private @NotNull final Document html;
    private final MainPage mainPage;
    public AGMainPageLoader(@NotNull Document document){
        this.html = document;
        this.mainPage = new MainPage();
    }

    public MainPage load() {
        this.loadTnsSlider();
        this.loadContentBlocks();
        return mainPage;
    }

    private void loadContentBlocks() {
        for(Element block:html.getElementsByClass("content-block")){
            Elements block_a;
            if((block_a=block.getElementsByClass("animes-list-item")).size()!=0){
                final Element title = block.selectFirst(".card-title, .card-header");
                this.loadBigList(block_a,title.text());
            }//if New Animes block
            else if((block_a=block.getElementsByClass("list-group")).size()!=0){
                final Element title = block.selectFirst(".card-title");
                this.loadListGroup(block_a,title!=null?title.text():"");
            }//container
            else if(block.child(0).hasClass("card") &&
                    (block_a=block.select(".list-group-item")).size()!=0){
                final Element title = block.selectFirst(".card-title"),
                        href = block.selectFirst(".card-header a");
                this.loadListGroupItems(block_a,title,href);
            }//list items
        }
    }

    private void loadListGroupItems(Elements elements, Element title, Element href) {
        ArrayList<OneAnime> answer = new ArrayList<>();

        for(Element element:elements){
            final OneAnime item = this.parseListGroupItem(element);
            if(item!=null)answer.add(item);
        }

        this.mainPage.rows.add(new RowList(new OneAnime.Link(title!=null?title.text():"",href!=null?href.attr("href"):""),answer));
    }

    private void loadListGroup(Elements blocks,final String title){
        Elements titles = blocks.select(".card-header, div[role='tab']");
        Elements items = blocks.select(".last-update-container, div[id^='slide-toggle-'], div[role='tabpanel']");
        int count = Math.min(titles.size(),items.size());
        ArrayList<RowList> answer = new ArrayList<>(count);
        for(int i=0;i<count;i++){
            Element currentTitle = titles.get(i),
                    currentItem = items.get(i);
            Elements data_items = currentItem.select(".last-update-item, .list-group-item");
            ArrayList<OneAnime> anime_items = new ArrayList<>(data_items.size());
            for(Element el:data_items) {
                OneAnime parsed_item = this.parseListGroupItem(el);
                if(parsed_item!=null)
                    anime_items.add(parsed_item);
            }
            String titleString = currentTitle.selectFirst("span").text();
            RowList item = new RowList(OneAnime.Link.noHref(titleString),anime_items);
            answer.add(item);
        }
        this.mainPage.containers.add(new Container(OneAnime.Link.noHref(title),answer));
    }

    private OneAnime parseListGroupItem(Element el) {
        OneAnime ans = new OneAnime(Config.HOST_ANIMEGO_ORG);
        Element image_element = el.selectFirst(".img-square, .lazy, .br-50, .anime-grid-lazy"),
                title_element = el.selectFirst(".last-update-title, .font-weight-600, .card-title"),
                poster_titles = el.selectFirst(".ml-3, .text-right");
        if(image_element==null) return null;
        ans.setCover( HtmlUtils.INSTANCE.getImageFromStyles(image_element));
        ans.title = title_element.text();
        if(poster_titles!=null){
            Elements children;
            while ((children = poster_titles.children()).size()<2){
                if(children.size()==0)break;
                poster_titles = children.get(0);
            }
            if(children.size()==2){
                OneAnime.DataPosterText data = ans.getDataPosterText();
                data.mainTitle = children.get(0).text();
                data.subTitle = children.get(1).text();
            }
        }
        else {
            Elements titles_2 = el.select(".seasons-item-info");
            if(titles_2.size()==2){
                OneAnime.DataPosterText data = ans.getDataPosterText();
                data.mainTitle = titles_2.get(0).text();
                data.subTitle = titles_2.get(1).text();
            }
        }
        String href;
        if((href = HtmlUtils.INSTANCE.parseHrefFromElement(el))!=null||
           (href = HtmlUtils.INSTANCE.parseHrefFromElement(title_element))!=null||
           (href = HtmlUtils.INSTANCE.parseHrefFromElement(title_element.parent()))!=null)
            ans.setPath(href);
        else return null;

        return ans;
    }

    private void loadBigList(Elements blocks,final String title) {
        final ArrayList<OneAnime> items = new ArrayList<>(blocks.size());
        for(Element bl:blocks) {
            Element data = bl.selectFirst(".media-body");
            if(data==null)continue;
            Element title_el = data.selectFirst("a"),
                    description = data.selectFirst(".description"),
                    cover = bl.selectFirst(".anime-list-lazy"),
                    genres = data.selectFirst(".anime-genre");
            if (title_el != null) {
                final OneAnime anime = new OneAnime(Config.HOST_ANIMEGO_ORG);
                anime.title = title_el.text();
                anime.setPath(title_el.attr("href"));
                AGAnimeLoader.loadGenresToAnime(genres,anime.getAttrs());
                if(description!=null)anime.description = HtmlUtils.INSTANCE.getTextWithNewLines(description);
                if(cover!=null)anime.setCover(cover.attr("data-original"));
                items.add(anime);
            }
        }
        final RowList list = new RowList(OneAnime.Link.noHref(title),items);
        if(mainPage.bigRow!=null || any(items,anime->anime.description!=null&&!anime.description.isEmpty())==null )//if no description found
            mainPage.rows.add(list);
        else mainPage.bigRow = list;
    }
    private void loadTnsSlider() {
        for(Element carousel:html.select(".carousel-horizontal-container")) {
            final Element title = carousel.selectFirst(".text-dark, .text__underline__link");
            final Elements items = carousel.select(".tns-item, .item");
            final RowList rowList = new RowList(new OneAnime.Link(title),new ArrayList<>(items.size()));
            for (Element el : items) {
                final Element cover = el.selectFirst(".anime-grid-lazy, .tns-lazy-img"),
                        element_title = el.selectFirst(".carousel-item-title, .card-title"),
                        element_link = el.selectFirst("a");
                final String path;
                if (element_title != null && cover != null &&
                        (path = !element_title.attr("href").isEmpty()?
                                element_title.attr("href"):
                                element_link!=null?element_link.attr("href"):null)!=null) {
                    final OneAnime anime = new OneAnime(Config.HOST_ANIMEGO_ORG);
                    anime.title = element_title.text();
                    anime.setPath(path);
                    anime.setCover(HtmlUtils.INSTANCE.getImageFromStyles(cover));
                    rowList.list.add(anime);
                }
            }
            mainPage.rows.add(rowList);
        }
    }
}
