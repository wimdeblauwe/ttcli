package io.github.wimdeblauwe.ttcli;

import java.io.IOException;

public interface LiveReloadInitStrategy {
    void execute(LiveReloadInitParameters parameters) throws IOException, InterruptedException;
}
