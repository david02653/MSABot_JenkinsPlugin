package ntou.david.dismessenger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import hudson.model.BuildListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeoutException;

public class RabbitControl {

    private static final String EXCHANGE_NAME = "jenkins";

    public static boolean sendMessage(String content, String routingKey, BuildListener listener){

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("140.121.197.130");
        factory.setPort(9050);

        StringWriter err = new StringWriter();

        try(Connection connection = factory.newConnection(); Channel channel = connection.createChannel()){
            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

            channel.basicPublish(EXCHANGE_NAME, routingKey, null, content.getBytes("UTF-8"));
            System.out.println("[x] Sent '" + routingKey + "':'"+ content + "'");
            listener.getLogger().println("sent: " + content);
            return true;
        }catch (IOException ioe){
            ioe.printStackTrace(new PrintWriter(err));
            listener.getLogger().println(err.toString());
        }catch (TimeoutException te){
            te.printStackTrace(new PrintWriter(err));
            listener.getLogger().println(err.toString());
        }
        return false;
    }
}
