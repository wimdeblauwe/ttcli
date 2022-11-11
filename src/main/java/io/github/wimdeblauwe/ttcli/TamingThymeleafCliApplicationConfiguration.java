package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrClient;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ImportRuntimeHints(TamingThymeleafCliApplicationRuntimeHints.class)
public class TamingThymeleafCliApplicationConfiguration {
    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("tt> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public SpringBootInitializrClient springBootInitializrClient() {
        WebClient webClient = WebClient.builder()
                                       .baseUrl("https://start.spring.io/")
                                       .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                                                                 .clientAdapter(WebClientAdapter.forClient(webClient))
                                                                 .build();
        return factory.createClient(SpringBootInitializrClient.class);
    }
}
