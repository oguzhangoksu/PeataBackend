package peata.backend.core;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "notifications-exchange";
    
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
