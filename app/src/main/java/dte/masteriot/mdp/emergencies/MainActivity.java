package dte.masteriot.mdp.emergencies;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//COMENTARIO DE ANDREA
//OTRO COMENTARIO
public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    //Comentario de kika
    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private ListView lv;
    ImageView imageView;
    String urls = " ";

    ArrayList<String> cameraNameList = new ArrayList<String>();
    ArrayList<String> cameraDescriptionList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urls = " ";
        //text =  (TextView) findViewById(R.id.KMLTextView);
        //btLoad = (Button) findViewById( R.id.readWebpage );
        //text.setText( "Click button to connect to " + URL_CAMERAS );

        lv = findViewById(R.id.listView);
        lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );


        imageView = findViewById(R.id.imageView);

        //gsonParse();
        xmlParse();

        lv.setOnItemClickListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,
                cameraNameList);

        lv.setAdapter(adapter);
    }

    public void xmlParse(){
        XmlPullParserFactory parserFactory;

        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = getAssets().open("CCTV.kml");

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);


            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("description".equals(elementName)) {
                            String cameraURL = parser.nextText();
                            cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                            cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4);
                            cameraDescriptionList.add( cameraURL );

                        }
                        else if ("Data".equals(elementName)) {
                            if(parser.getAttributeValue(null,"name").equals("Nombre")){
                                parser.nextTag();
                                String cameraName = parser.nextText();
                                cameraNameList.add(cameraName);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.toString();
        }
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SparseBooleanArray checked= lv.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            if ( checked.valueAt(i) ) {
                int pos = checked.keyAt(i);
                urls = cameraDescriptionList.get(pos);

            }
        }
        DownloadImages task1 = new DownloadImages();
        task1.execute( urls );
    }

    private class DownloadImages extends AsyncTask<String, Void, String > {

        private String contentType = "";
        Bitmap bitmap;


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
            imageView.setImageBitmap( bitmap );

        }
    }




}
