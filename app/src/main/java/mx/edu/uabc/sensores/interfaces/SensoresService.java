package mx.edu.uabc.sensores.interfaces;

import com.tinder.scarlet.Stream;
import com.tinder.scarlet.ws.Receive;
import com.tinder.scarlet.ws.Send;

import mx.edu.uabc.sensores.models.EventMessage;

public interface SensoresService {

    @Send
    void sendMessage(EventMessage message);

    @Receive
    Stream<EventMessage> observe();
}