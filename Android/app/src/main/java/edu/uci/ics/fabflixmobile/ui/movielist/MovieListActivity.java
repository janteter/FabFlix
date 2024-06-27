package edu.uci.ics.fabflixmobile.ui.movielist;

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
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    private String getRequestURL;
    // page number
    private TextView pageNumberView;
    private String pageNumber = "1";
    private String baseURL = new BaseURL().getBaseURL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());
        final Button nextButton = binding.next;
        final Button prevButton = binding.prev;
        pageNumberView = binding.pageNumberView;
        nextButton.setOnClickListener(view -> {
            try {
                next();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        prevButton.setOnClickListener(view -> {
            try {
                prev();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

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

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // request type is POST
        // TODO: should parse the json response to redirect to appropriate functions
        final StringRequest resultsRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/" + getRequestURL,
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        JSONArray returnedJSONArray = dataJson.getJSONArray("results");

                        final ArrayList<Movie> movies = new ArrayList<>();

                        for(int i = 0; i < returnedJSONArray.length(); i++) {
                            JSONObject currentMovieData = returnedJSONArray.getJSONObject(i);

                            movies.add(constructMovieFromJson(currentMovieData));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            String getSingleMovieRequestURL = "single-movie?id=" + movie.getId();
                            finish();
                            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            SingleMoviePage.putExtra("get_request_url", getSingleMovieRequestURL);
                            // activate the list page.
                            startActivity(SingleMoviePage);
                        });

                        pageNumber = "1";
                        this.pageNumberView.setText(pageNumber);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                }, error -> Log.d("login.error", error.toString())) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(resultsRequest);
    }


    @SuppressLint("SetTextI18n")
    public void prev() throws JSONException {

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        JSONObject previousPageParams = new JSONObject();
        previousPageParams.put("pageChangeValue", "-1");
        previousPageParams.put("page_number", pageNumber);
        final JsonObjectRequest prevRequest = new JsonObjectRequest(
                Request.Method.POST,
                baseURL + "/api/sort?pageChangeValue=-1&page_number=" + pageNumber,
                previousPageParams,
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        boolean validPageChange = dataJson.getBoolean("valid_page_change");
                        String newPage = dataJson.getString("new_page_num");

                        if (validPageChange) {
                            Log.d("Valid previous page change", jsonStringFormat);
                            // Mark the flag to Get request to MoviePageServlet as true
                            StringRequest getMoviesRequest = createGetMoviesRequestViaPageChange(newPage);
                            queue.add(getMoviesRequest);
                            // Recreate the page
                        } else {

                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                },
                error -> { Log.d("login.error", error.toString()); });
        // important: queue.add is where the login request is actually sent
        queue.add(prevRequest);
    }

    @SuppressLint("SetTextI18n")
    public void next() throws JSONException {

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        JSONObject previousPageParams = new JSONObject();
        previousPageParams.put("pageChangeValue", "1");
        previousPageParams.put("page_number", pageNumber);
        final JsonObjectRequest prevRequest = new JsonObjectRequest(
                Request.Method.POST,
                baseURL + "/api/sort?pageChangeValue=1&page_number=" + pageNumber,
                previousPageParams,
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        boolean validPageChange = dataJson.getBoolean("valid_page_change");
                        String newPage = dataJson.getString("new_page_num");

                        if (validPageChange) {
                            Log.d("Valid previous page change", jsonStringFormat);
                            // Mark the flag to Get request to MoviePageServlet as true
                            StringRequest getMoviesRequest = createGetMoviesRequestViaPageChange(newPage);
                            queue.add(getMoviesRequest);
                            // Recreate the page
                        } else {

                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                },
                error -> { Log.d("login.error", error.toString()); });
        // important: queue.add is where the login request is actually sent
        queue.add(prevRequest);
    }

    // /api/results?page=PAGE_NUMBER_HERE
    private StringRequest createGetMoviesRequestViaPageChange(String newPageNumber) {
        StringRequest requestToReturn = new StringRequest(Request.Method.GET,
                baseURL + "/api/results?page=" + newPageNumber,
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        JSONArray returnedJSONArray = dataJson.getJSONArray("results");

                        final ArrayList<Movie> movies = new ArrayList<>();

                        for(int i = 0; i < returnedJSONArray.length(); i++) {
                            JSONObject currentMovieData = returnedJSONArray.getJSONObject(i);

                            movies.add(constructMovieFromJson(currentMovieData));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            String getSingleMovieRequestURL = "single-movie?id=" + movie.getId();
                            finish();
                            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            SingleMoviePage.putExtra("get_request_url", getSingleMovieRequestURL);
                            // activate the list page.
                            startActivity(SingleMoviePage);
                        });

                        this.pageNumber = newPageNumber;
                        this.pageNumberView.setText(pageNumber);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                }, error -> Log.d("getting_movies_after_page_change.error", error.toString())) {
        };
        return requestToReturn;
    }



    private Movie constructMovieFromJson (JSONObject currentJsonMovieObj) throws JSONException {
        String movieName = currentJsonMovieObj.getString("movie_title");
        String movieYear  = currentJsonMovieObj.getString("movie_year");
        String movieDirector = currentJsonMovieObj.getString("movie_director");
        ArrayList<String> movieGenres = new ArrayList<>();

        JSONArray genresFromJSON = currentJsonMovieObj.getJSONArray("movie_genres");
        for(int i = 0; i < genresFromJSON.length(); i++) {
            String genre = genresFromJSON.getString(i);
            movieGenres.add(genre);
        }

        ArrayList<String> movieStars = new ArrayList<>();
        JSONArray starsFromJSON = currentJsonMovieObj.getJSONArray("movie_stars");
        for(int i = 0; i < starsFromJSON.length(); i++) {
            String star = starsFromJSON.getString(i);
            movieStars.add(star);
        }

        String movieId = currentJsonMovieObj.getString("movie_id");

        return new Movie(movieName, movieYear, movieDirector, movieGenres, movieStars, movieId);
    }
}