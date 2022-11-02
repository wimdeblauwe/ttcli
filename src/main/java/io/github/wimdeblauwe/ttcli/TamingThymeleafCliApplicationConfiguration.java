package io.github.wimdeblauwe.ttcli;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@ImportRuntimeHints(TamingThymeleafCliApplicationRuntimeHints.class)
public class TamingThymeleafCliApplicationConfiguration {
    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("tt> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
