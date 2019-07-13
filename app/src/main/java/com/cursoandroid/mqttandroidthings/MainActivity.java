package com.cursoandroid.mqttandroidthings;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = "Things";
    private static final String topic = "WilliamST/boton";
    private static final String hello = "Hello world! Android Things conectada.";
    private static final String click = "click!";
    private static final int qos = 1;
    private static final String broker = "tcp://broker.hivemq.com:1883";
    private static final String clientId = "Test134561995";

    private final String PIN_BUTTON = "BCM23";
    private Gpio mButtonGpio;
    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PeripheralManager service = PeripheralManager.getInstance();
            mButtonGpio = service.openGpio(PIN_BUTTON);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(mCallback);

            client = new MqttClient(broker, clientId, new MemoryPersistence());
            Log.i(TAG, "Conectando al broker " + broker);
            client.connect();
            Log.i(TAG, "Conectado");
            Log.i(TAG, "Publicando mensaje: " + hello);
            MqttMessage message = new MqttMessage(hello.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
            Log.i(TAG, "Mensaje publicado");
        } catch (MqttException e) {
            Log.e(TAG, "Error en MQTT.", e);
        } catch (IOException e) {
            Log.e(TAG, "Error en PeripheralIO API", e);
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.i(TAG, "Botón pulsado!");
                Log.i(TAG, "Publicando mensaje: " + click);
                MqttMessage message = new MqttMessage(click.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
                Log.i(TAG, "Mensaje publicado");
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error en PeripheralIO API", e);
            }
        }
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

}
