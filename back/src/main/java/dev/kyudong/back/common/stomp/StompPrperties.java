package dev.kyudong.back.common.stomp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Slf4j
@Getter
@ConfigurationProperties(prefix = "stomp")
public class StompPrperties {

	private final String prefix;
	private final Long sessionTime;

	@ConstructorBinding
	public StompPrperties(String prefix, Long sessionTime) {
		this.prefix = prefix;
		this.sessionTime = sessionTime;
	}

}
