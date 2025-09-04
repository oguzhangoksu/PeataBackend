package peata.backend.core;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "notifications-exchange";
    public static final String CHAT_EXCHANGE = "chat-exchange";
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }
    @Bean
    public Queue chatPushQueue() {
        return QueueBuilder.durable("chat.push.q")
            .withArgument("x-dead-letter-exchange", CHAT_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "chat.push.retry")
            .build();
    }
    @Bean
    public Queue chatPushRetry30sQueue() {
        return QueueBuilder.durable("chat.push.retry.30s")
            .withArgument("x-dead-letter-exchange", CHAT_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "chat.message.created")
            .withArgument("x-message-ttl", 30_000)
            .build();
    }

  @Bean
    public Queue chatPushDlq() {
        return QueueBuilder.durable("chat.push.dlq").build();
    }

    @Bean
    public Binding chatPushBinding(TopicExchange chatExchange, Queue chatPushQueue) {
        return BindingBuilder.bind(chatPushQueue).to(chatExchange).with("chat.message.created");
    }

    @Bean
    public Binding chatRetryBinding(TopicExchange chatExchange, Queue chatPushRetry30sQueue) {
        return BindingBuilder.bind(chatPushRetry30sQueue).to(chatExchange).with("chat.push.retry");
    }

    @Bean
    public Binding chatDlqBinding(TopicExchange chatExchange, Queue chatPushDlq) {
        return BindingBuilder.bind(chatPushDlq).to(chatExchange).with("chat.push.dlq");
    }

    @Bean(name = "chatRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory chatRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        configurer.configure(f, connectionFactory);
        f.setConcurrentConsumers(3);
        f.setMaxConcurrentConsumers(10);
        f.setPrefetchCount(30);
        f.setDefaultRequeueRejected(false); 
        return f;
    }





    // Declare the topic exchange for notifications
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Declare the RabbitAdmin to manage RabbitMQ components
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true); // Ensure auto-declaration at startup
        return rabbitAdmin;
    }

    // Method to create a durable queue dynamically
    public Queue createDurableQueue(String queueName) {
        return new Queue(queueName, true); // Durable queues persist even after RabbitMQ restarts
    }

    // Method to create bindings between a queue and the topic exchange
    public Binding createBinding(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(topicExchange()).with(routingKey);
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange("email-exchange");
    }
    @Bean
    public TopicExchange registerEmailExchange() {
        return new TopicExchange("register-email-exchange");
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("email-queue");
    }
    @Bean
    public Queue registerEmailQueue() {
        return new Queue("register-email-queue");
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with("email-routing-key");
    }

    @Bean
    public Binding registerEmailBinding(Queue registerEmailQueue, TopicExchange registerEmailExchange) {
        return BindingBuilder.bind(registerEmailQueue).to(registerEmailExchange).with("register-email-routing-key");
    }
    

    
    
}
