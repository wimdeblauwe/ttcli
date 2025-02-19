package io.github.wimdeblauwe.ttcli.livereload;

import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;

public interface TailwindCssSpecializedLiveReloadInitService extends LiveReloadInitService {
    boolean isTailwindVersionOf(TailwindVersion tailwindVersion, Class<? extends LiveReloadInitService> liveReloadInitServiceClass);
}
