package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectCreationParameters;

import java.nio.file.Path;

public record InitParameters(Path basePath, SpringBootProjectCreationParameters springBootProjectCreationParameters) {
}
