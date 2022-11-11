package io.github.wimdeblauwe.ttcli.boot;


import io.github.wimdeblauwe.ttcli.util.ZipUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

class SpringBootInitializrClientManualTest {

    @Test
    void testGetMetadata() {
        SpringBootInitializrClient client = springBootInitializrClient();
        InitializrMetadata metadata = client.getMetadata();
        System.out.println("metadata = " + metadata);
    }

    @Test
    void testGenerate() throws IOException {
        SpringBootInitializrClient client = springBootInitializrClient();
        byte[] o = client.generateProject("maven-project",
                                          "2.7.5",
                                          "com.example",
                                          "demo",
                                          "Demo",
                                          Set.of("web", "thymeleaf"));
        ZipUtil.unzip(o, Path.of("target", "test-unzip"));
    }

    private SpringBootInitializrClient springBootInitializrClient() {
        WebClient webClient = WebClient.builder()
                                       .baseUrl("https://start.spring.io/")
                                       .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                                                                 .clientAdapter(WebClientAdapter.forClient(webClient))
                                                                 .build();
        return factory.createClient(SpringBootInitializrClient.class);
    }

}
