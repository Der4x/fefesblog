package de.fwpm.android.fefesblog.data;

import android.content.SharedPreferences;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

import de.fwpm.android.fefesblog.BlogPost;

/**
 * Created by alex on 19.01.18.
 */

public class HtmlParser {

    public static ArrayList<BlogPost> parseHtml(Document doc, boolean search) {

        Elements dates = doc.select("h3");

        Elements listsOfPosts = doc.select("body > ul");

        Elements div = doc.select("body > div");
        String nextUrl = new String();

        if( div.size() != 0 && !div.select("a[href]").isEmpty() && !search) {
            nextUrl = div.select("a[href]").get(0).attr("abs:href");
        }

        ArrayList<BlogPost> allPosts = new ArrayList<>();

        int counter = 0;

        for(Element listOfPosts : listsOfPosts) {

            Elements posts = listOfPosts.children();

            for (Element post : posts) {

                Elements links = post.select("a[href]");

                BlogPost blogPost = new BlogPost();
                blogPost.type = BlogPost.TYPE_DATA;

                blogPost.setHtmlText(post.toString());
                blogPost.setText(post.text());
                blogPost.setDate(dates.get(counter).text());

                HashMap<String, String> postLinks= new HashMap<String, String>();

                for(Element link : links) {

                    if(link.text().equals("[l]")) {

                        String tempurl = link.attr("abs:href");

                        if(!tempurl.contains("blog.fefe.de")) {
                            tempurl = tempurl.replace("https://", "https://blog.fefe.de/");
                        }

                        blogPost.setUrl(tempurl);
                    }
                }

                allPosts.add(blogPost);

            }

            counter++;

        }

        if(!search && allPosts.size() > 1) allPosts.get(allPosts.size()-1).setNextUrl(nextUrl);

        return allPosts;
    }
}
