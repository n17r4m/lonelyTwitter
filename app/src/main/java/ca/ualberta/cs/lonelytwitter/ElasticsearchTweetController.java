package ca.ualberta.cs.lonelytwitter;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * Created by esports on 2/17/16.
 */
public class ElasticsearchTweetController {

    private static JestDroidClient client;

    private static void verifyConfig(){
        if(client == null){
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder("http://cmput301.softwareprocess.es:8080");
            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }
    }

    public static class GetTweetsTask extends AsyncTask<String,Void,ArrayList<Tweet>> {

        @Override
        protected ArrayList<Tweet> doInBackground(String... params) {
            verifyConfig();

            ArrayList<Tweet> tweets = new ArrayList<Tweet>();

            // Assume only one search parameter.

            String query = "{\"size\":50}";
            if (params[0] != null) {
                String param = new Gson().toJson(params[0]);
                query = "{\"size\":50, \"query\": {\"match\": {\"message\": " + param + "}}}";
            } else {

            }

            Search search = new Search.Builder(query).addIndex("testing").addType("tweet").build();



            try {
                SearchResult execute = client.execute(search);
                if (execute.isSucceeded()){
                    List<NormalTweet> foundTweets = execute.getSourceAsObjectList(NormalTweet.class);
                    tweets.addAll(foundTweets);
                } else {
                    Log.e("WARN", "Our search for tweets failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return tweets;
        }
    }

    public static ArrayList<Tweet> getTweets(String search){
        try {
            return new GetTweetsTask().execute(search).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class AddTweetTask extends AsyncTask<Tweet,Void,Void> {

        @Override
        protected Void doInBackground(Tweet... params) {
            verifyConfig();

            for(Tweet tweet : params) {
                Index index = new Index.Builder(tweet).index("testing").type("tweet").build();
                try {
                    DocumentResult execute = client.execute(index);
                    if (execute.isSucceeded()) {
                        tweet.setId(execute.getId());
                    } else {
                        Log.e("WARN", "Our insert of tweet failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public static void addTweet(Tweet tweet){
        new AddTweetTask().execute(tweet);
    }

}
