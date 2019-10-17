package dte.masteriot.mdp.emergencies;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTT {
    MqttAndroidClient mqttAndroidClient;
    String serverUri;
    String clientId;
    private static String MQTTAPIKey;
    String subscriptionTopic;

    float measurement;

    Context context;


    MQTT(MainActivity ma, Context contextMain){
        mqttAndroidClient = ma.mqttAndroidClient;
        serverUri = ma.serverUri;
        clientId = ma.clientId;
        subscriptionTopic = ma.subscriptionTopic;
        MQTTAPIKey = ma.getMQTTAPIKey();
        measurement = ma.measurement;
        context = contextMain;
    }

    public void mqttConnection(){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {

                }
            }

            @Override
            public void connectionLost(Throwable cause) { }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //addToHistory("Incoming message: " + new String(message.getPayload()));
                String msg = new String(message.getPayload());
                measurement = Float.parseFloat(msg);

                //Mirar que funciona
                Log.d("Debug", new String(message.getPayload()));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) { }
        });



        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        // mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setUserName( "user" );
        mqttConnectOptions.setPassword(MQTTAPIKey.toCharArray());

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }



    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Debug", "Subscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                }
            });
        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }



}
