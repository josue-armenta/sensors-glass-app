package mx.edu.uabc.sensores.clients;

import com.tinder.scarlet.Scarlet;
import com.tinder.scarlet.Stream;
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter;
import com.tinder.scarlet.websocket.okhttp.OkHttpClientUtils;

import java.util.concurrent.TimeUnit;

import mx.edu.uabc.sensores.interfaces.SensoresService;
import mx.edu.uabc.sensores.models.EventMessage;
import okhttp3.OkHttpClient;

public class SensoresClient {

    private static final String SENSORS_URL = "wss://pacific-depths-88167.herokuapp.com";

    private static SensoresClient instance;
    private SensoresService sensoresService;

    private SensoresClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        sensoresService = new Scarlet.Builder()
                .webSocketFactory(OkHttpClientUtils.newWebSocketFactory(okHttpClient, SENSORS_URL))
                .addMessageAdapterFactory(new GsonMessageAdapter.Factory())
                .build().create(SensoresService.class);

    }

    public static SensoresClient getInstance() {
        if (instance == null)
            instance = new SensoresClient();
        return instance;
    }

    public Stream<EventMessage> observe() {
        return sensoresService.observe();
    }

    public void sendMessage(EventMessage message) {
        sensoresService.sendMessage(message);
    }

}
