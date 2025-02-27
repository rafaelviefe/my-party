package com.myparty.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
public class RabbitMQConfig {

	@Value("${rabbitmq.exchange}")
	private String exchange;

	@Value("${rabbitmq.queue}")
	private String queue;

	@Value("${rabbitmq.routingKey}")
	private String routingKey;

	@Bean
	public Queue queue() {
		return new Queue(queue, true);
	}

	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(exchange);
	}

	@Bean
	public Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}
}
