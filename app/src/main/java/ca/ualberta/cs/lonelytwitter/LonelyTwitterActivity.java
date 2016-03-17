package ca.ualberta.cs.lonelytwitter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LonelyTwitterActivity extends Activity {

    private EditText bodyText;
    private ListView oldTweetsList;

    private ArrayList<Tweet> tweets = new ArrayList<Tweet>();
    private ArrayAdapter<Tweet> adapter;

    private Button saveButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public ArrayAdapter<Tweet> getAdapter() {
        return adapter;
    }

    private ImageButton pictureButton;
    private Bitmap thumbnail;

    static final int REQUEST_IMAGE_CAPTURE = 1231;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bodyText = (EditText) findViewById(R.id.tweetMessage);
        oldTweetsList = (ListView) findViewById(R.id.tweetsList);


        // http://developer.android.com/training/camera/photobasics.html
        pictureButton = (ImageButton) findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = bodyText.getText().toString();
                NormalTweet latestTweet = new NormalTweet(text);

                tweets.add(latestTweet);

                latestTweet.addThumbnail(thumbnail);

                getTweets();
                // Add the tweet to Elasticsearch
                ElasticsearchTweetController.AddTweetTask addTweetTask = new ElasticsearchTweetController.AddTweetTask();
                addTweetTask.execute(latestTweet);
                tweets.add(0, latestTweet);
                // http://stackoverflow.com/questions/11835251/remove-image-resource-of-imagebutton

                bodyText.setText("");
                pictureButton.setImageResource(android.R.color.transparent);
                thumbnail = null;

                setResult(RESULT_OK);


                adapter.notifyDataSetChanged();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void getTweets(){
        // Get the latest tweets from Elasticsearch
        ElasticsearchTweetController.GetTweetsTask getTweetsTask = new ElasticsearchTweetController.GetTweetsTask();
//        getTweetsTask.execute("test");
        getTweetsTask.execute("");
        try {
            tweets = new ArrayList<Tweet>();
            tweets.addAll(getTweetsTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//        adapter = new ArrayAdapter<Tweet>(this, R.layout.list_item, tweets);
        // Binds tweet list with view, so when our array updates, the view updates with it
        adapter = new TweetAdapter(this, tweets); /* NEW! */
        oldTweetsList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        getTweets();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "LonelyTwitter Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ca.ualberta.cs.lonelytwitter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "LonelyTwitter Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ca.ualberta.cs.lonelytwitter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // http://developer.android.com/training/camera/photobasics.html

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = intent.getExtras();
            thumbnail = (Bitmap) extras.get("data");
            pictureButton.setImageBitmap(thumbnail);
        }
    }
}
