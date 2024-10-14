package io.github.wimdeblauwe.ttcli.livereload;

public interface TailwindCssSpecializedLiveReloadInitService extends LiveReloadInitService {
    boolean isTailwindVersionOf(Class<? extends LiveReloadInitService> liveReloadInitServiceClass);
}
