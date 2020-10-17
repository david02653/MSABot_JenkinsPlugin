package ntou.david.dismessenger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitControl {

    private static final String EXCHANGE_NAME = "jenkins";

    public static boolean sendMessage(String content, String routingKey){

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("140.121.197.130");
        factory.setPort(9050);

        try(Connection connection = factory.newConnection(); Channel channel = connection.createChannel()){
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            channel.basicPublish(EXCHANGE_NAME, routingKey, null, content.getBytes("UTF-8"));
            System.out.println("[x] Sent '" + routingKey + "':'"+ content + "'");
            return true;
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (TimeoutException te){
            te.printStackTrace();
        }
        return false;
    }
}
