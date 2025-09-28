package dev.kyudong.back.common.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	// Exchanges
	private static final String MEDIA_EXCHANGE = "media.exchange";
	private static final String DLX_EXCHANGE = "dlx.exchange";

	// --- Queue Names ---
	private static final String VIDEO_PROCESS_QUEUE = "media.video.process.queue";
	private static final String IMAGE_PROCESS_QUEUE = "media.image.process.queue";
	private static final String PROGRESS_UPDATE_QUEUE = "progress.update.queue";

	// --- DLQ Names ---
	private static final String VIDEO_PROCESS_DLQ = "media.video.process.dlq";
	private static final String IMAGE_PROCESS_DLQ = "media.image.process.dlq";

	// --- Routing Keys ---
	private static final String VIDEO_PROCESS_ROUTING_KEY = "media.video.process";
	private static final String IMAGE_PROCESS_ROUTING_KEY = "media.image.process";
	private static final String PROGRESS_UPDATE_ROUTING_KEY = "progress.update";

	private static final String DLQ_VIDEO_ROUTING_KEY = "dlq.media.video.process";
	private static final String DLQ_IMAGE_ROUTING_KEY = "dlq.media.image.process";

	@Bean
	public TopicExchange mediaExchange() {
		return new TopicExchange(MEDIA_EXCHANGE);
	}

	@Bean
	public TopicExchange deadLetterExchange() {
		return new TopicExchange(DLX_EXCHANGE);
	}

	@Bean
	public Queue videoProcessQueue() {
		return QueueBuilder.durable(VIDEO_PROCESS_QUEUE)
				.withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
				.withArgument("x-dead-letter-routing-key", DLQ_VIDEO_ROUTING_KEY)
				.build();
	}

	@Bean
	public Binding videoProcessBinding() {
		return BindingBuilder.bind(videoProcessQueue()).to(mediaExchange()).with(VIDEO_PROCESS_ROUTING_KEY + ".#");
	}
	@Bean
	public Queue videoProcessDeadLetterQueue() {
		return new Queue(VIDEO_PROCESS_DLQ);
	}
	@Bean
	public Binding videoProcessDlqBinding() {
		return BindingBuilder.bind(videoProcessDeadLetterQueue()).to(deadLetterExchange()).with(DLQ_VIDEO_ROUTING_KEY);
	}

	@Bean
	public Queue imageProcessQueue() {
		return QueueBuilder.durable(IMAGE_PROCESS_QUEUE)
				.withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
				.withArgument("x-dead-letter-routing-key", DLQ_IMAGE_ROUTING_KEY)
				.build();
	}
	@Bean
	public Binding imageProcessBinding() {
		return BindingBuilder.bind(imageProcessQueue()).to(mediaExchange()).with(IMAGE_PROCESS_ROUTING_KEY + ".#");
	}
	@Bean
	public Queue imageProcessDeadLetterQueue() {
		return new Queue(IMAGE_PROCESS_DLQ);
	}
	@Bean
	public Binding imageProcessDlqBinding() {
		return BindingBuilder.bind(imageProcessDeadLetterQueue()).to(deadLetterExchange()).with(DLQ_IMAGE_ROUTING_KEY);
	}

	@Bean
	public Queue progressQueue() {
		return new Queue(PROGRESS_UPDATE_QUEUE, true);
	}
	@Bean
	public Binding bindingProgressQueue() {
		return BindingBuilder.bind(progressQueue()).to(mediaExchange()).with(PROGRESS_UPDATE_ROUTING_KEY + ".#");
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

}
