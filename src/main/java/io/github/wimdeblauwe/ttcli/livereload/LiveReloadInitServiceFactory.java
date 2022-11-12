package io.github.wimdeblauwe.ttcli.livereload;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveReloadInitServiceFactory {
    private final List<LiveReloadInitService> initServices;

    public LiveReloadInitServiceFactory(List<LiveReloadInitService> initServices) {
        this.initServices = initServices;
    }

    public List<LiveReloadInitService> getInitServices() {
        return initServices;
    }

    public LiveReloadInitService getInitService(String initServiceId) {
        return initServices.stream().filter(service -> service.getId().equals(initServiceId))
                           .findAny()
                           .orElseThrow(() -> new IllegalArgumentException("Not a known service: " + initServiceId));
    }
}
