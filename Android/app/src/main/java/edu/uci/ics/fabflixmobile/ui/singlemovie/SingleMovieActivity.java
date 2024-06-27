package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.constants.BaseURL;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import android.widget.TextView;
import android.widget.Button;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainPageActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import edu.uci.ics.fabflixmobile.ui.movielist.MovieListViewAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private String getRequestURL;
    private String baseURL = new BaseURL().getBaseURL();
    private final ArrayList<Movie> singleMovie = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                getRequestURL = null;
            } else {
                getRequestURL = extras.getString("get_request_url");
            }
        } else {
            getRequestURL = savedInstanceState.getSerializable("get_request_url", String.class);
        }

        getMovie();
//        MovieListViewAdapter adapter = new MovieListViewAdapter(this, singleMovie);
//        ListView listView = findViewById(R.id.list);
//        listView.setAdapter(adapter);
    }

    private void getMovie() {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        StringRequest getSingleMovieDetailsRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/" + getRequestURL,
                response -> {
                    try {
                        JSONArray dataArray = new JSONArray(response);
                        JSONObject dataJson = dataArray.getJSONObject(0);
                        String movie_title = dataJson.getString("movie_name");
                        String movie_year = dataJson.getString("movie_year");
                        String movie_director = dataJson.getString("movie_director");
                        JSONArray movie_stars = dataJson.getJSONArray("movie_stars");
                        JSONArray movie_genres = dataJson.getJSONArray("movie_genres");

                        ArrayList<String> starsToInsert = new ArrayList<>();
                        for (int i = 0; i < movie_stars.length(); ++i) {
                            starsToInsert.add(movie_stars.getString(i));
                        }

                        ArrayList<String> genresToInsert = new ArrayList<>();
                        for (int i = 0; i < movie_genres.length(); ++i) {
                            genresToInsert.add(movie_genres.getString(i));
                        }

                        Movie movieToDisplay =
                                new Movie(movie_title, movie_year, movie_director,
                                        genresToInsert, starsToInsert, "ID DOesn't matter");

                        this.singleMovie.add(movieToDisplay);
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, singleMovie);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                },
                error -> Log.d("search.error", error.toString()));

        queue.add(getSingleMovieDetailsRequest);
    }
}
