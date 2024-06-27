package edu.uci.ics.fabflixmobile.ui.login;
import edu.uci.ics.fabflixmobile.data.constants.BaseURL;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainPageActivity;
import org.json.JSONException;
import org.json.JSONObject;
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
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private TextView message;
    private String baseURL = new BaseURL().getBaseURL();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        email = binding.email;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> {
            try {
                login();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void login() throws JSONException {

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/login?type=user&captcha=no",
                response -> {
                    try {
                        String jsonStringFormat = String.valueOf(response);
                        JSONObject dataJson = new JSONObject(jsonStringFormat);
                        String returnStatus = dataJson.getString("status");

                        if (returnStatus.equals("success")) {
                            Log.d("login.success", jsonStringFormat);
                            //Complete and destroy login activity once successful
                            finish();
                            // initialize the activity(page)/destination
                            Intent MainPage = new Intent(LoginActivity.this, MainPageActivity.class);
                            // activate the list page.
                            startActivity(MainPage);
                        } else {
                            Log.d("login.failure", jsonStringFormat);
                            message.setText("Incorrect Login Information, Try Again!");
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    ;
                }, error -> Log.d("login.error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("email", email.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}