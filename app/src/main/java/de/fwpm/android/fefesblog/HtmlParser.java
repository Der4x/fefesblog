package de.fwpm.android.fefesblog;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 19.01.18.
 */

public class HtmlParser {


    private static final String TAG = "HTMLPARSER";

    public static ArrayList<BlogPost> parseHtml(Document doc) {

        Elements dates = doc.select("h3");
        Elements listsOfPosts = doc.select("ul");

        ArrayList<BlogPost> allPosts = new ArrayList<>();

        int counter = 0;

        for(Element listOfPosts : listsOfPosts) {

            Elements posts = listOfPosts.select("li");

            for (Element post : posts) {

                Elements links = post.select("a[href]");

                Log.d(TAG, post.toString());

                BlogPost blogPost = new BlogPost();
                blogPost.type = BlogPost.TYPE_DATA;

                blogPost.setHtmlText(post.toString());
                blogPost.setText(post.text());
                blogPost.setDate(dates.get(counter).text());

                HashMap<String, String> postLinks= new HashMap<String, String>();

                for(Element link : links) {

                    if(link.text().equals("[l]")) {
                        blogPost.setUrl(link.attr("abs:href"));
                    } else {
                        postLinks.put(link.text(), link.attr("abs:href"));
                    }
                }

                blogPost.setLinks(postLinks);


                allPosts.add(blogPost);

            }

            counter++;

        }

        Log.d(TAG, "" + counter);
        return allPosts;

    }

}
