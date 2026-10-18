package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.IdAndName;
import io.github.wimdeblauwe.ttcli.boot.SpringInitializrSingleSelect;
import io.github.wimdeblauwe.ttcli.deps.HtmxVersion;
import io.github.wimdeblauwe.ttcli.deps.HtmxWebDependency;
import io.github.wimdeblauwe.ttcli.deps.ShadleafWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Validates the values of the {@code init} command-line options against the same rules that the interactive
 * flow applies. The valid values for web dependencies, Tailwind dependencies and live reload implementations
 * come from the corresponding beans.
 */
@Component
public class InitOptionsValidator {

    /**
     * Value for {@code --web-deps} and {@code --tailwind-deps} to explicitly select nothing.
     */
    public static final String NONE = "none";

    private final List<WebDependency> webDependencies;
    private final List<TailwindDependency> tailwindDependencies;
    private final LiveReloadInitServiceFactory liveReloadInitServiceFactory;

    public InitOptionsValidator(List<WebDependency> webDependencies,
                                List<TailwindDependency> tailwindDependencies,
                                LiveReloadInitServiceFactory liveReloadInitServiceFactory) {
        this.webDependencies = webDependencies;
        this.tailwindDependencies = tailwindDependencies;
        this.liveReloadInitServiceFactory = liveReloadInitServiceFactory;
    }

    public String validateArtifact(String artifact) {
        if (!artifact.matches("[A-Za-z0-9._-]+")) {
            throw new InvalidOptionException("Invalid value '%s' for --artifact. Use only letters, digits, '.', '_' and '-'."
                    .formatted(artifact));
        }
        return artifact;
    }

    /**
     * Matches against the id or the display name of the start.spring.io values, as the picker offers both.
     *
     * @return the id of the matching value
     */
    public String validateInitializrValue(String option, String value, SpringInitializrSingleSelect select) {
        List<IdAndName> values = select.getValues();
        return values.stream()
                .filter(v -> v.id().equalsIgnoreCase(value) || v.name().equalsIgnoreCase(value))
                .findFirst()
                .map(IdAndName::id)
                .orElseThrow(() -> invalid(option, value, values.stream().map(IdAndName::id).toList()));
    }

    public String validateTemplateEngine(String value) {
        return match("--template-engine", value, List.of(TemplateEngineType.TYPE_THYMELEAF, TemplateEngineType.TYPE_JTE),
                Function.identity());
    }

    public boolean validateLayoutDialect(String value, String templateEngineId) {
        if (!TemplateEngineType.TYPE_THYMELEAF.equals(templateEngineId)) {
            throw conflict("--thymeleaf-layout-dialect", "--template-engine " + templateEngineId,
                    "the Thymeleaf Layout Dialect only applies to Thymeleaf");
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes" -> true;
            case "false", "no" -> false;
            default -> throw invalid("--thymeleaf-layout-dialect", value, List.of("yes", "no"));
        };
    }

    public List<String> availableLiveReloadIds(TemplateEngineType templateEngineType) {
        return liveReloadInitServiceFactory.getNormalServices().stream()
                .filter(s -> s.isApplicableForTemplateEngine(templateEngineType))
                .map(LiveReloadInitService::getId)
                .toList();
    }

    public String validateLiveReload(String value, TemplateEngineType templateEngineType) {
        List<String> applicable = availableLiveReloadIds(templateEngineType);
        boolean known = liveReloadInitServiceFactory.getNormalServices().stream()
                .anyMatch(s -> normalize(s.getId()).equals(normalize(value)));
        boolean applicableToEngine = applicable.stream().anyMatch(id -> normalize(id).equals(normalize(value)));
        if (known && !applicableToEngine) {
            throw conflict("--live-reload " + value, "--template-engine " + templateEngineType.id(),
                    "valid live reload implementations with " + templateEngineType.id() + ": " + String.join(", ", applicable));
        }
        return match("--live-reload", value, applicable, Function.identity());
    }

    public PackageManager validatePackageManager(String value, boolean packageManagerUsed, String liveReloadId) {
        if (!packageManagerUsed) {
            throw conflict("--package-manager", "--live-reload " + liveReloadId,
                    "a package manager is only used by npm-based and vite, or by dev-tools-based together with tailwind-css");
        }
        return match("--package-manager", value, Arrays.asList(PackageManager.values()), p -> p.name().toLowerCase(Locale.ROOT));
    }

    public List<WebDependency> validateWebDependencies(String value,
                                                       String springBootVersion,
                                                       TemplateEngineType templateEngineType) {
        List<WebDependency> selected = matchAll("--web-deps", value, webDependencies, WebDependency::id);
        for (WebDependency webDependency : selected) {
            if (!webDependency.isApplicable(springBootVersion, templateEngineType)) {
                throw conflict("--web-deps " + webDependency.id(),
                        "--template-engine " + templateEngineType.id() + " with Spring Boot " + springBootVersion,
                        webDependency.displayName() + " is not available for this combination"
                                + (webDependency instanceof ShadleafWebDependency ? " (it needs Thymeleaf and Spring Boot 4.1 or newer)" : ""));
            }
        }
        return selected;
    }

    public HtmxVersion validateHtmxVersion(String value,
                                           List<WebDependency> selectedWebDependencies,
                                           String springBootVersion,
                                           TemplateEngineType templateEngineType) {
        if (selectedWebDependencies.stream().noneMatch(HtmxWebDependency.class::isInstance)) {
            throw conflict("--htmx-version", "--web-deps", "htmx must be one of the web dependencies");
        }
        HtmxVersion version = match("--htmx-version", value, Arrays.asList(HtmxVersion.values()), InitOptionsValidator::versionNumber);
        if (version == HtmxVersion.VERSION_4 && !HtmxWebDependency.supportsHtmx4(springBootVersion, templateEngineType)) {
            throw conflict("--htmx-version 4", "--template-engine " + templateEngineType.id() + " with Spring Boot " + springBootVersion,
                    "htmx 4 with Thymeleaf needs Spring Boot 4 (use --htmx-version 2)");
        }
        return version;
    }

    public TailwindVersion validateTailwindVersion(String value, boolean hasTailwindCss, boolean hasShadleaf) {
        if (!hasTailwindCss) {
            throw conflict("--tailwind-version", "--web-deps", "tailwind-css must be one of the web dependencies");
        }
        TailwindVersion version = match("--tailwind-version", value, Arrays.asList(TailwindVersion.values()), InitOptionsValidator::versionNumber);
        if (hasShadleaf && version != TailwindVersion.VERSION_4) {
            throw conflict("--tailwind-version 3", "--web-deps shadleaf", "Shadleaf always uses Tailwind 4");
        }
        return version;
    }

    public List<TailwindDependency> validateTailwindDependencies(String value, boolean hasTailwindCss) {
        if (!hasTailwindCss) {
            throw conflict("--tailwind-deps", "--web-deps", "tailwind-css must be one of the web dependencies");
        }
        return matchAll("--tailwind-deps", value, tailwindDependencies, TailwindDependency::id);
    }

    // "VERSION_4" -> "4"
    private static String versionNumber(Enum<?> version) {
        return version.name().substring(version.name().indexOf('_') + 1);
    }

    private <T> List<T> matchAll(String option, String value, Collection<T> candidates, Function<T, String> idOf) {
        if (NONE.equalsIgnoreCase(value)) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (String part : value.split(",")) {
            if (part.isBlank()) {
                continue;
            }
            T match = match(option, part.trim(), candidates, idOf);
            if (!result.contains(match)) {
                result.add(match);
            }
        }
        return result;
    }

    /**
     * Matches ignoring case and punctuation, either completely or as a unique prefix
     * (so {@code tailwind} finds {@code tailwind-css} and {@code alpine} finds {@code alpinejs}).
     */
    private <T> T match(String option, String value, Collection<T> candidates, Function<T, String> idOf) {
        String normalized = normalize(value);
        List<T> exact = candidates.stream().filter(c -> normalize(idOf.apply(c)).equals(normalized)).toList();
        if (exact.size() == 1) {
            return exact.getFirst();
        }
        List<T> prefix = candidates.stream()
                .filter(c -> !normalized.isEmpty() && normalize(idOf.apply(c)).startsWith(normalized))
                .toList();
        if (prefix.size() == 1) {
            return prefix.getFirst();
        }
        throw invalid(option, value, candidates.stream().map(idOf).toList());
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static InvalidOptionException invalid(String option, String value, Collection<String> validValues) {
        return new InvalidOptionException("Invalid value '%s' for %s. Valid values: %s"
                .formatted(value, option, String.join(", ", validValues)));
    }

    private static InvalidOptionException conflict(String option, String other, String reason) {
        return new InvalidOptionException("%s is not allowed together with %s: %s.".formatted(option, other, reason));
    }
}
