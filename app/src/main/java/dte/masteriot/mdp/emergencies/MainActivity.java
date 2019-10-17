package dte.masteriot.mdp.emergencies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    float measurement;
    Context context;

    //MQTT variables
    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://mqtt.thingspeak.com:1883";
    String clientId = "MDPMQTTExample";
    final String subscriptionTopic = "channels/886928/subscribe/fields/field1/JLQ98B3XBAQ2RP9Y"; //Clave de lectura
    final String publishMessage = "10";

    // Keys for accessing ThingSpeak:
    private static final String UserAPIKey = "YOUR_API_KEY_HERE";
    private static final String MQTTAPIKey = "GX0XE33JWDK52TO9";


    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private ListView lv;
    ImageView imageView;
    String urls = " ";

    ArrayList<String> cameraNameList = new ArrayList<String>();
    ArrayList<String> cameraDescriptionList = new ArrayList<String>();

    public static String getMQTTAPIKey() {
        return MQTTAPIKey;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.listView);
        lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        imageView = findViewById(R.id.imageView);

        context = getApplicationContext();

        xmlParse();

        lv.setOnItemClickListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,
                cameraNameList);
        lv.setAdapter(adapter);


        MQTT mqtt = new MQTT(this, context);
        mqtt.mqttConnection();

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
        //DownloadImages task1 = new DownloadImages();
        //task1.execute( urls );


        //Le pasamos al constructor un objeto de tipo MainActivity
        //asi tenemos las variables del main en la clase DownloadImages
        DownloadImages taskImages = new DownloadImages(this);
        taskImages.execute( urls );
    }


}
