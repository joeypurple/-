package com.example.android.booklistb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Tag for Log Messages
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    // Tag for API URL
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=1&q=";

    Button mSearchButton;

    EditText mSearchField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchButton = (Button) findViewById(R.id.search_button);
        mSearchField = (EditText) findViewById(R.id.search_editText);

        ArrayList<Books> books = new ArrayList<>();

        // Create the adapter to convert the array to views
        BooksAdapter adapter = new BooksAdapter(this, books);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = mSearchField.getText().toString();
                String queryString = GOOGLE_BOOKS_BASE_URL + userInput;
                URL url = createUrl(queryString);

                Log.v("EditText", url.toString());

                if (userInput == null || userInput.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter search terms.", Toast.LENGTH_SHORT).show();
                } else {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute(url);
                }
            }
        });
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList> {

        @Override
        protected ArrayList<Books> doInBackground(URL... urls) {

            //Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHTTPRequest(urls[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "MainActivity " + "IOException", e);
            }

            //Extract relevant fields from the JSON response and create an {@link Event} object
            ArrayList<Books> books = extractFeatureFromJson(jsonResponse);

            // Return the {@link Books} object as the result of the {@link BookAsyncTask}
            return books;
        }
    }

    /**
     * Returns new URL object from the given String URL.
     */
    private URL createUrl(String stringUrl) {

        URL url = null;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }


    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private String makeHTTPRequest(URL url) throws IOException {
        String jsonResponse = "";

        // if url is null, then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            // if the request was successful (response code 200);
            // then read the input stream and parse the response
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem receiving book JSON results", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                //function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private ArrayList<Books> extractFeatureFromJson(String booksJSON) {

        ArrayList<Books> bookList = new ArrayList<>();

        // if JSON string is empty or null, return early
        if (TextUtils.isEmpty(booksJSON)) {
            return null;
        }
        try {
            JSONObject baseJsonResponse = new JSONObject(booksJSON);
            JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

            // If there are results in the features array
            if (itemsArray.length() > 0) {
                //Extract out the first feature
                JSONObject firstFeature = itemsArray.getJSONObject(0);
                JSONObject items = firstFeature.getJSONObject("volumeInfo");

                //Extract out the title, time, and tsunami values
                String title = items.getString("title");
                String authors = items.getString("authors");

                //Create a new {@link Event} object
                Books books = new Books(title, authors);
                //Add the book to the array
                bookList.add(books);
            } else if (itemsArray.length() == 0) {
                Toast.makeText(MainActivity.this, "No results found.", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }
        return bookList;
    }
}
