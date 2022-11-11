package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InitService {
    private final SpringBootInitializrService initializrService;

    public InitService(SpringBootInitializrService initializrService) {
        this.initializrService = initializrService;
    }

    public void initialize(InitParameters parameters) throws IOException {
        initializrService.generate(parameters.basePath(),
                                   parameters.springBootProjectCreationParameters());
    }
}
