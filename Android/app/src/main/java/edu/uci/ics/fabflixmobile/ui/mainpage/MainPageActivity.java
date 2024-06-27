package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.constants.BaseURL;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainpageBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainPageActivity extends AppCompatActivity {
    private EditText fulltextQuery;

    private TextView message;

    private String baseURL = new BaseURL().getBaseURL();
//    private final String host = "10.0.2.2";
//    private final String port = "8443";
//    private final String domain = "cs122b_fabflix_war";
//    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainpageBinding binding = ActivityMainpageBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        final Button searchButton = binding.searchButton;
        fulltextQuery = binding.search;
        message = binding.message;

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> {
            try {
                search();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void search() throws JSONException {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/index?typeOfSearch=fulltext",
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        boolean validStatus = dataJson.getBoolean("valid");
                        String movieListGetRequest = dataJson.getString("movie_page_get_url");

                        if (validStatus) {
                            Log.d("search.success", jsonStringFormat);
//                            message.setText("sending uri to movielistactivity " + movieListGetRequest);

                            //Complete and destroy login activity once successful
                            finish();
                            // initialize the activity(page)/destination
                            Intent MovieListPage = new Intent(this, MovieListActivity.class);
                            MovieListPage.putExtra("get_request_url", movieListGetRequest);
                            // activate the list page.
                            startActivity(MovieListPage);
                        } else {
                            Log.d("search.failure", jsonStringFormat);
                            message.setText("Invalid Search, Try Again!");
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                }, error -> Log.d("search.error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("fulltext-query", fulltextQuery.getText().toString());
                System.out.println(params.get("fulltext-query"));
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}
