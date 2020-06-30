package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);


        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data!");
                populateHomeTimeLine();
            }
        });
        // Find the RV
        rvTweets = findViewById(R.id.rvTweets);


        // Initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // RV view setup: layout manager and the adapter
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };

        // Adds the scroll listener to the RV
        rvTweets.addOnScrollListener(scrollListener);

        populateHomeTimeLine();

    }

    private void loadMoreData() {
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for load more data: " + json.toString());
                // Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try{
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);

                    // Append the new data objects to the existing set of items inside the array of items
                    // Notify the adapter of the new items made with `notifyItemRangeInserted()`
                    adapter.addAll(tweets);

                } catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to load more data", throwable);
            }
        }, tweets.get(tweets.size() - 1).id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose) {
            // Compose icon has been selected
            Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();

            // Navigate to the compose activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateHomeTimeLine(){
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try{
                    Log.i(TAG, "onSuccess" + json.toString());

                    // Resets and re-populates RV with latest tweets
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));

                    //Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);


                } catch(JSONException e){
                    Log.e(TAG, "Json Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG,"onFailure", throwable);
            }
        });
    }
}