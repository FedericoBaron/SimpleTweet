package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity=User.class, parentColumns="id", childColumns="userId"))
public class Tweet {

    @PrimaryKey
    @ColumnInfo
    public long id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @ColumnInfo
    public long userId;

    @ColumnInfo
    public String imageUrl;

    @Ignore
    public User user;

    // Empty constructor needed for parcel
    public Tweet(){}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");// Notify the adapter of the new items made with `notifyItemRangeInserted()`
        tweet.id = jsonObject.getLong("id");
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        if(jsonObject.has("extended_entities")){
            Log.i("TWEET", jsonObject.getJSONObject("extended_entities").toString());
            JSONObject entities = jsonObject.getJSONObject("extended_entities");
                JSONArray media = entities.getJSONArray("media");
                Log.i("TWEET", "HERES THE MEDIA: " + media);
                tweet.imageUrl = media.getJSONObject(0).getString("media_url_https");
                Log.i("TWEET", "HERE'S THE MEDIA URL: " + tweet.imageUrl);
        }
        else{
            tweet.imageUrl = "";
        }
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }
}
