package io.github.wimdeblauwe.ttcli.boot;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Set;

public interface SpringBootInitializrClient {
    @GetExchange("/metadata/client")
    InitializrMetadata getMetadata();

    @GetExchange("/starter.zip")
    byte[] generateProject(@RequestParam("type") String type,
                           @RequestParam("bootVersion") String bootVersion,
                           @RequestParam("groupId") String groupId,
                           @RequestParam("artifactId") String artifactId,
                           @RequestParam("name") String name,
                           @RequestParam("dependencies") Set<String> dependencies);
}
