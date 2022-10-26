package io.github.wimdeblauwe.ttcli;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import org.xmlbeam.XBProjector;
import org.xmlbeam.config.DefaultXMLFactoriesConfig;

@Configuration
public class TamingThymeleafCliApplicationConfiguration {
    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("tt> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public XBProjector projectionFactory() {

        DefaultXMLFactoriesConfig config = new DefaultXMLFactoriesConfig();
        config.setNamespacePhilosophy(DefaultXMLFactoriesConfig.NamespacePhilosophy.AGNOSTIC);
        config.setOmitXMLDeclaration(false);
        config.setPrettyPrinting(false);

        return new XBProjector(config, XBProjector.Flags.TO_STRING_RENDERS_XML);
    }
}
