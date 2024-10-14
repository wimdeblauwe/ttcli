package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrClient;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.style.FigureSettings;
import org.springframework.shell.style.StyleSettings;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ImportRuntimeHints(TamingThymeleafCliApplicationRuntimeHints.class)
public class TamingThymeleafCliApplicationConfiguration {

    @Bean
    Theme myTheme() {
        return new Theme() {
            @Override
            public String getName() {
                return "taming-thymeleaf-theme";
            }

            @Override
            public ThemeSettings getSettings() {
                return new ThemeSettings() {
                    @Override
                    public StyleSettings styles() {
                        return new TamingThymeleafStyleSettings();
                    }

                    @Override
                    public FigureSettings figures() {
                        return FigureSettings.defaults();
                    }
                };
            }
        };
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("tt> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public SpringBootInitializrClient springBootInitializrClient() {
        RestClient restClient = RestClient.builder()
                                          .defaultHeader("Accept", "application/vnd.initializr.v2.2+json")
                                          .baseUrl("https://start.spring.io/")
                                          .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                                                                 .exchangeAdapter(RestClientAdapter.create(restClient))
                                                                 .build();
        return factory.createClient(SpringBootInitializrClient.class);
    }
}
