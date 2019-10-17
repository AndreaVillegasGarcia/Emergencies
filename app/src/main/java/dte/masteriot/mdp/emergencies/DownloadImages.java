package dte.masteriot.mdp.emergencies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImages extends AsyncTask<String, Void, String > {

    private String contentType = "";
    Bitmap bitmap;
    ImageView imgView;

    DownloadImages(MainActivity ma){
        imgView = ma.imageView;

    }


    protected String doInBackground(String... urls) {
        String response = "";

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            contentType = urlConnection.getContentType();
            InputStream is = urlConnection.getInputStream();

            // Content type should be "application/vnd.google-earth.kml+xml"
            if (contentType.toString()
                    .contentEquals("image/jpeg")) {
                InputStreamReader reader = new InputStreamReader(is);
                bitmap = BitmapFactory.decodeStream(is);

            } else {
                response = contentType + " not processed";
            }
        } catch (Exception e) {
            response = e.toString();
        }
        urlConnection.disconnect();
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        //Toast.makeText(MainActivity.this, contentType, Toast.LENGTH_SHORT).show();
        imgView.setImageBitmap( bitmap );

    }
}
