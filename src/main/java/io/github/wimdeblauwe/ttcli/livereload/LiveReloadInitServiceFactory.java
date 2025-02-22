package io.github.wimdeblauwe.ttcli.livereload;

import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.function.Predicate.not;

@Component
public class LiveReloadInitServiceFactory {
    private final List<LiveReloadInitService> allServices;
    private final List<LiveReloadInitService> normalServices;
    private final List<TailwindCssSpecializedLiveReloadInitService> tailwindCssSpecializedServices;

    public LiveReloadInitServiceFactory(List<LiveReloadInitService> allServices) {
        this.allServices = allServices;
        this.normalServices = allServices.stream().filter(not(TailwindCssSpecializedLiveReloadInitService.class::isInstance)).toList();
        this.tailwindCssSpecializedServices = allServices.stream().filter(TailwindCssSpecializedLiveReloadInitService.class::isInstance)
                                                         .map(s -> (TailwindCssSpecializedLiveReloadInitService) s)
                                                         .toList();
    }

    public List<LiveReloadInitService> getAllServices() {
        return allServices;
    }

    public List<LiveReloadInitService> getNormalServices() {
        return normalServices;
    }

    public List<TailwindCssSpecializedLiveReloadInitService> getTailwindCssSpecializedServices() {
        return tailwindCssSpecializedServices;
    }

    public LiveReloadInitService getInitService(String initServiceId,
                                                TailwindVersion tailwindVersion,
                                                boolean hasTailwindCssWebDependency) {
        return allServices.stream().filter(service -> service.getId().equals(initServiceId))
                          .findAny()
                .map(s -> hasTailwindCssWebDependency ? getCorrespondingTailwindCssSpecializedLiveReloadInitService(tailwindVersion, s) : s)
                          .orElseThrow(() -> new IllegalArgumentException("Not a known service: " + initServiceId));
    }

    private TailwindCssSpecializedLiveReloadInitService getCorrespondingTailwindCssSpecializedLiveReloadInitService(TailwindVersion tailwindVersion, LiveReloadInitService initService) {
        return getTailwindCssSpecializedServices().stream()
                .filter(t -> t.isTailwindVersionOf(tailwindVersion, initService.getClass()))
                                                  .findFirst()
                                                  .orElseThrow(() -> new IllegalArgumentException("Could not find the corresponding tailwind version of " + initService.getClass()));
    }
}
