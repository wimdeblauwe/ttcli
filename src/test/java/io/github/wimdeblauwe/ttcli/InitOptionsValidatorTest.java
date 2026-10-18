package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.IdAndName;
import io.github.wimdeblauwe.ttcli.boot.SpringInitializrSingleSelect;
import io.github.wimdeblauwe.ttcli.deps.*;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.tailwind.*;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InitOptionsValidatorTest {

    private static final TemplateEngineType THYMELEAF = new TemplateEngineType.Thymeleaf(true);
    private static final TemplateEngineType JTE = new TemplateEngineType.Jte();

    private final InitOptionsValidator validator = new InitOptionsValidator(
            List.of(new AlpineWebDependency(), new BootstrapWebDependency(), new HtmxWebDependency(),
                    new ShadleafWebDependency(), new ShoelaceWebDependency(), new TailwindCssWebDependency()),
            List.of(new FormsTailwindDependency(), new TypographyTailwindDependency(),
                    new ContainerQueriesTailwindDependency(), new DaisyUiTailwindDependency()),
            new LiveReloadInitServiceFactory(List.of(
                    liveReload("dev-tools-based", true, false),
                    liveReload("npm-based", true, false),
                    liveReload("vite", true, true))));

    private static LiveReloadInitService liveReload(String id, boolean thymeleaf, boolean jte) {
        LiveReloadInitService service = mock(LiveReloadInitService.class);
        when(service.getId()).thenReturn(id);
        when(service.isApplicableForTemplateEngine(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArgument(0) instanceof TemplateEngineType.Jte ? jte : thymeleaf);
        return service;
    }

    private static SpringInitializrSingleSelect select(String... ids) {
        SpringInitializrSingleSelect select = new SpringInitializrSingleSelect();
        select.setValues(java.util.Arrays.stream(ids).map(id -> new IdAndName(id, "Name " + id)).toList());
        return select;
    }

    @Test
    void initializrValueMatchesIdOrName() {
        SpringInitializrSingleSelect select = select("4.1.1", "3.5.7");
        assertThat(validator.validateInitializrValue("--boot-version", "3.5.7", select)).isEqualTo("3.5.7");
        assertThat(validator.validateInitializrValue("--boot-version", "name 4.1.1", select)).isEqualTo("4.1.1");
    }

    @Test
    void initializrValueRejectedWithValidValues() {
        assertThatThrownBy(() -> validator.validateInitializrValue("--java-version", "7", select("21", "25")))
                .isInstanceOf(InvalidOptionException.class)
                .hasMessage("Invalid value '7' for --java-version. Valid values: 21, 25");
    }

    @Test
    void artifactMustBeSafeDirectoryName() {
        assertThat(validator.validateArtifact("my-app_1.0")).isEqualTo("my-app_1.0");
        assertThatThrownBy(() -> validator.validateArtifact("../evil")).isInstanceOf(InvalidOptionException.class);
    }

    @Test
    void templateEngine() {
        assertThat(validator.validateTemplateEngine("JTE")).isEqualTo("jte");
        assertThatThrownBy(() -> validator.validateTemplateEngine("freemarker"))
                .hasMessage("Invalid value 'freemarker' for --template-engine. Valid values: thymeleaf, jte");
    }

    @Test
    void layoutDialect() {
        assertThat(validator.validateLayoutDialect("no", "thymeleaf")).isFalse();
        assertThat(validator.validateLayoutDialect("Yes", "thymeleaf")).isTrue();
        assertThatThrownBy(() -> validator.validateLayoutDialect("maybe", "thymeleaf"))
                .hasMessageContaining("Valid values: yes, no");
        assertThatThrownBy(() -> validator.validateLayoutDialect("yes", "jte"))
                .hasMessageContaining("--thymeleaf-layout-dialect is not allowed together with --template-engine jte");
    }

    @Test
    void liveReload() {
        assertThat(validator.validateLiveReload("vite", THYMELEAF)).isEqualTo("vite");
        assertThat(validator.validateLiveReload("devtools", THYMELEAF)).isEqualTo("dev-tools-based");
        assertThat(validator.availableLiveReloadIds(JTE)).containsExactly("vite");
        assertThatThrownBy(() -> validator.validateLiveReload("nope", THYMELEAF))
                .hasMessage("Invalid value 'nope' for --live-reload. Valid values: dev-tools-based, npm-based, vite");
    }

    @Test
    void liveReloadNotAvailableForJte() {
        assertThatThrownBy(() -> validator.validateLiveReload("npm-based", JTE))
                .isInstanceOf(InvalidOptionException.class)
                .hasMessageContaining("--live-reload npm-based is not allowed together with --template-engine jte")
                .hasMessageContaining("vite");
    }

    @Test
    void packageManager() {
        assertThat(validator.validatePackageManager("pnpm", true, "vite")).isEqualTo(PackageManager.PNPM);
        assertThatThrownBy(() -> validator.validatePackageManager("yarn", true, "vite"))
                .hasMessage("Invalid value 'yarn' for --package-manager. Valid values: npm, pnpm");
        assertThatThrownBy(() -> validator.validatePackageManager("npm", false, "dev-tools-based"))
                .hasMessageContaining("--package-manager is not allowed together with --live-reload dev-tools-based");
    }

    @Test
    void webDependenciesComeFromBeansAndAcceptPrefixes() {
        List<WebDependency> result = validator.validateWebDependencies("htmx, alpine,tailwind,htmx", "4.1.1", THYMELEAF);
        assertThat(result).extracting(WebDependency::id).containsExactly("htmx", "alpinejs", "tailwind-css");
    }

    @Test
    void webDependenciesNone() {
        assertThat(validator.validateWebDependencies("none", "4.1.1", THYMELEAF)).isEmpty();
    }

    @Test
    void webDependenciesInvalidListsValidValues() {
        assertThatThrownBy(() -> validator.validateWebDependencies("htmx,react", "4.1.1", THYMELEAF))
                .hasMessage("Invalid value 'react' for --web-deps. Valid values: alpinejs, bootstrap, htmx, shadleaf, shoelace, tailwind-css");
    }

    @Test
    void shadleafOnlyWithThymeleafAndBoot41() {
        assertThat(validator.validateWebDependencies("shadleaf", "4.1.1", THYMELEAF)).hasSize(1);
        assertThatThrownBy(() -> validator.validateWebDependencies("shadleaf", "4.0.5", THYMELEAF))
                .hasMessageContaining("--web-deps shadleaf is not allowed together with --template-engine thymeleaf with Spring Boot 4.0.5");
        assertThatThrownBy(() -> validator.validateWebDependencies("shadleaf", "4.1.1", JTE))
                .hasMessageContaining("--template-engine jte");
    }

    @Test
    void htmxVersion() {
        List<WebDependency> htmx = List.of(new HtmxWebDependency());
        assertThat(validator.validateHtmxVersion("4", htmx, "4.1.1", THYMELEAF)).isEqualTo(HtmxVersion.VERSION_4);
        assertThat(validator.validateHtmxVersion("2", htmx, "3.5.7", THYMELEAF)).isEqualTo(HtmxVersion.VERSION_2);
        assertThat(validator.validateHtmxVersion("4", htmx, "3.5.7", JTE)).isEqualTo(HtmxVersion.VERSION_4);
    }

    @Test
    void htmx4NotAllowedWithThymeleafAndBoot3() {
        assertThatThrownBy(() -> validator.validateHtmxVersion("4", List.of(new HtmxWebDependency()), "3.5.7", THYMELEAF))
                .hasMessageContaining("--htmx-version 4 is not allowed together with --template-engine thymeleaf with Spring Boot 3.5.7");
    }

    @Test
    void htmxVersionRequiresHtmx() {
        assertThatThrownBy(() -> validator.validateHtmxVersion("4", List.of(new AlpineWebDependency()), "4.1.1", THYMELEAF))
                .hasMessage("--htmx-version is not allowed together with --web-deps: htmx must be one of the web dependencies.");
        assertThatThrownBy(() -> validator.validateHtmxVersion("3", List.of(new HtmxWebDependency()), "4.1.1", THYMELEAF))
                .hasMessage("Invalid value '3' for --htmx-version. Valid values: 2, 4");
    }

    @Test
    void tailwindVersion() {
        assertThat(validator.validateTailwindVersion("3", true, false)).isEqualTo(TailwindVersion.VERSION_3);
        assertThatThrownBy(() -> validator.validateTailwindVersion("3", true, true))
                .hasMessageContaining("--tailwind-version 3 is not allowed together with --web-deps shadleaf");
        assertThatThrownBy(() -> validator.validateTailwindVersion("4", false, false))
                .hasMessageContaining("tailwind-css must be one of the web dependencies");
        assertThatThrownBy(() -> validator.validateTailwindVersion("5", true, false))
                .hasMessage("Invalid value '5' for --tailwind-version. Valid values: 3, 4");
    }

    @Test
    void tailwindDependencies() {
        assertThat(validator.validateTailwindDependencies("forms,daisy", true))
                .extracting(TailwindDependency::id).containsExactly("forms", "daisy-ui");
        assertThat(validator.validateTailwindDependencies("none", true)).isEmpty();
        assertThatThrownBy(() -> validator.validateTailwindDependencies("forms", false))
                .hasMessageContaining("--tailwind-deps is not allowed together with --web-deps");
        assertThatThrownBy(() -> validator.validateTailwindDependencies("bulma", true))
                .hasMessage("Invalid value 'bulma' for --tailwind-deps. Valid values: forms, typography, container-queries, daisy-ui");
    }
}
