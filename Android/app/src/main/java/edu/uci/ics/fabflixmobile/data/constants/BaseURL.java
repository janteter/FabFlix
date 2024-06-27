package edu.uci.ics.fabflixmobile.data.constants;

public class BaseURL {
    private final String protocol = "https";
    // private final String host = "10.0.2.2"; FOR LOCALHOST
    private final String host = "18.191.133.196"; // REPLACE WITH AWS IP
    private final String port = "8443";
    // private final String domain = "cs122b_fabflix_war"; FOR LOCALHOST
    private final String domain = "cs122b_fabflix";
    private final String baseURL = protocol + "://" + host + ":" + port + "/" + domain;
    public String getBaseURL() { return baseURL; }
}
