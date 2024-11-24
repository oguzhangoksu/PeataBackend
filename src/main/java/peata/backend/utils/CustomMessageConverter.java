package peata.backend.utils;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class CustomMessageConverter implements MessageConverter {

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(message.getBody());
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new MessageConversionException("Error deserializing message", e);
        }
    }

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        // Gerekirse, nesneyi AMQP mesajına dönüştürme işlemi yapılabilir
        // Örneğin, bir HashMap ise:
        // return new Message(object.toString().getBytes(), messageProperties);
        throw new UnsupportedOperationException("Serialization not supported");
    }
}