package io.github.wimdeblauwe.ttcli.boot;


import io.github.wimdeblauwe.ttcli.util.ZipUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Disabled // To be run manually
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
        byte[] o = client.generateProject(SpringBootProjectType.MAVEN.value(),
                                          "2.7.5",
                "17",
                                          "com.example",
                                          "demo",
                                          "Demo",
                                          Set.of("web", "thymeleaf"));
        ZipUtil.unzip(o, Path.of("target", "test-unzip"));
    }

    private SpringBootInitializrClient springBootInitializrClient() {
        RestClient webClient = RestClient.builder()
                                         .baseUrl("https://start.spring.io/")
                                         .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                                                                 .exchangeAdapter(RestClientAdapter.create(webClient))
                                                                 .build();
        return factory.createClient(SpringBootInitializrClient.class);
    }

}
