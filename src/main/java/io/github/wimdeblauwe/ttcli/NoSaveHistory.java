package io.github.wimdeblauwe.ttcli;

import org.jline.reader.impl.history.DefaultHistory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NoSaveHistory extends DefaultHistory {
    @Override
    public void save() throws IOException {

    }
}
