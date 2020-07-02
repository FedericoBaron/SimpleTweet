package com.codepath.apps.restclienttemplate.activities;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {

    private static final String KEY_LINK = "link";
    private static final String TAG = "TweetDetailsActivity";

    // The movie to display
    private Tweet tweet;

    private Button retweet;
    private ImageView attachedImage;
    private TextView tvScreenName;
    private TextView relativeDate;
    private TextView tvBody;
    private TextView tvScreenUsername;
    private ImageView ivProfileImage;

    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        client = TwitterApp.getRestClient(this);

        // the view objects
        // Find the views
        retweet = findViewById(R.id.retweet);
        attachedImage = findViewById(R.id.attachedImage);
        tvScreenName = findViewById(R.id.tvScreenName);
        relativeDate = findViewById(R.id.relativeDate);
        tvBody = findViewById(R.id.tvBody);
        tvScreenUsername = findViewById(R.id.tvScreenUsername);
        ivProfileImage = findViewById(R.id.ivProfileImage);


        // Unwrap the movie passed in via intent, using its simple name as a key
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        wireUI();

    }

    // It connects the UI and the movie information
    private void wireUI()
    {
        tvBody.setText(tweet.getBody());
        tvScreenUsername.setText("@" + tweet.getUser().getScreenName());
        tvScreenName.setText(tweet.getUser().getName());
        relativeDate.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        Glide.with(this).load(tweet.getUser().getProfileImageUrl()).into(ivProfileImage);
        if(!tweet.imageUrl.isEmpty()){
            attachedImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.getImageUrl()).into(attachedImage);
            Log.i(TAG, "The image is: " + tweet.getImageUrl());
        }
        else
        {
            attachedImage.setVisibility(View.GONE);
        }
    }

    private void retweetListener() {
        // Listener for thumbnail click
        retweet.setOnClickListener(new View.OnClickListener() {
            // Handler for add button click
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void postTweet(){
        // Send an API request to post the tweet
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to load more data", throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }


    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
