package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShadleafWebDependencyTest {

    private final ShadleafWebDependency shadleafWebDependency = new ShadleafWebDependency();

    @Test
    void testIsApplicable_thymeleafWithSpringBoot41() {
        assertThat(shadleafWebDependency.isApplicable("4.1.0", new TemplateEngineType.Thymeleaf(true))).isTrue();
        assertThat(shadleafWebDependency.isApplicable("4.1.1-SNAPSHOT", new TemplateEngineType.Thymeleaf(false))).isTrue();
    }

    @Test
    void testIsApplicable_notForSpringBoot40OrOlder() {
        assertThat(shadleafWebDependency.isApplicable("4.0.5", new TemplateEngineType.Thymeleaf(true))).isFalse();
        assertThat(shadleafWebDependency.isApplicable("3.5.7", new TemplateEngineType.Thymeleaf(true))).isFalse();
    }

    @Test
    void testIsApplicable_notForJte() {
        assertThat(shadleafWebDependency.isApplicable("4.1.0", new TemplateEngineType.Jte())).isFalse();
    }

    @Test
    void testGetMavenDependencies() {
        assertThat(shadleafWebDependency.getMavenDependencies("4.1.0", new TemplateEngineType.Thymeleaf(true)))
                .containsExactly(new MavenDependency("io.github.wimdeblauwe", "shadleaf-spring-boot-starter", ShadleafWebDependency.SHADLEAF_VERSION));
    }

    @Test
    void testRemoveAlpineIfShadleafIsSelected_removesAlpine() {
        AlpineWebDependency alpine = new AlpineWebDependency();
        TailwindCssWebDependency tailwind = new TailwindCssWebDependency();

        List<WebDependency> result = ShadleafWebDependency.removeAlpineIfShadleafIsSelected(List.of(alpine, shadleafWebDependency, tailwind));

        assertThat(result).containsExactly(shadleafWebDependency, tailwind);
    }

    @Test
    void testRemoveAlpineIfShadleafIsSelected_keepsAlpineWithoutShadleaf() {
        AlpineWebDependency alpine = new AlpineWebDependency();

        List<WebDependency> result = ShadleafWebDependency.removeAlpineIfShadleafIsSelected(List.of(alpine));

        assertThat(result).containsExactly(alpine);
    }
}
