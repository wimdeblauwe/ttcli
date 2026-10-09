package io.github.wimdeblauwe.ttcli.livereload.helper;

import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.util.ConsoleOutput;
import org.jsoup.nodes.Comment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.wimdeblauwe.ttcli.deps.VersionConstants.FRONTEND_MAVEN_PLUGIN_VERSION;

public final class NpmHelper {

    /**
     * Native packages whose install scripts pnpm 10+ blocks by default. Pre-approving these in
     * `pnpm-workspace.yaml` makes the first build run cleanly without manual approval.
     */
    public static final List<String> PNPM_ALLOW_BUILDS_DEFAULTS = List.of(
            "@tailwindcss/oxide",
            "lightningcss",
            "esbuild"
    );

    /**
     * Writes a `pnpm-workspace.yaml` next to the `package.json` that allows the build scripts of
     * {@link #PNPM_ALLOW_BUILDS_DEFAULTS}. pnpm 11+ no longer reads the `pnpm` field of `package.json`
     * and uses `allowBuilds`; `onlyBuiltDependencies` is kept as well so pnpm 10 keeps working.
     */
    public static void applyPnpmAllowBuilds(Path packageJsonDir) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("# Packages that are allowed to run their install scripts (pnpm 11+)\n");
        builder.append("allowBuilds:\n");
        PNPM_ALLOW_BUILDS_DEFAULTS.forEach(dependency -> builder.append("  '").append(dependency).append("': true\n"));
        builder.append("\n");
        builder.append("# Same list for pnpm 10, which does not support `allowBuilds` yet\n");
        builder.append("onlyBuiltDependencies:\n");
        PNPM_ALLOW_BUILDS_DEFAULTS.forEach(dependency -> builder.append("  - '").append(dependency).append("'\n"));
        Files.writeString(packageJsonDir.resolve("pnpm-workspace.yaml"), builder.toString());
    }

    public static Map<String, String> rewriteScriptsForPackageManager(PackageManager packageManager,
                                                                      Map<String, String> scripts) {
        if (packageManager == PackageManager.NPM) {
            return scripts;
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        scripts.forEach((name, value) -> result.put(name, value.replace("npm run ", packageManager.executable() + " run ")));
        return result;
    }

    public static void updateMavenPom(MavenPomReaderWriter mavenPomReaderWriter,
                                      InstalledApplicationVersions versions,
                                      boolean addReleaseProfile) throws IOException, InterruptedException {
        ConsoleOutput.println(ConsoleOutput.Icon.CONFIGURE, "Updating Maven pom.xml");
        PackageManager packageManager = versions.packageManager();
        String installGoal = packageManager.frontendMavenPluginInstallGoal();
        String runGoal = packageManager.frontendMavenPluginRunGoal();
        String versionPropertyName = packageManager.mavenVersionProperty();

        mavenPomReaderWriter.updateResources(resources -> resources.append("""
                                                                                   <resource>
                                                                                       <directory>src/main/resources</directory>
                                                                                       <excludes>
                                                                                           <exclude>**/*.html</exclude>
                                                                                           <exclude>**/*.css</exclude>
                                                                                           <exclude>**/*.js</exclude>
                                                                                           <exclude>**/*.svg</exclude>
                                                                                       </excludes>
                                                                                   </resource>
                                                                                   """));
        mavenPomReaderWriter.updateProperties(properties -> {
            properties.appendChild(new Comment(" Maven plugins "));
            properties.appendElement("frontend-maven-plugin.version").text(FRONTEND_MAVEN_PLUGIN_VERSION);
            properties.appendElement("frontend-maven-plugin.nodeVersion").text(versions.nodeVersion());
            properties.appendElement(versionPropertyName).text(versions.packageManagerVersion());
        });

        String installVersionConfig = packageManager == PackageManager.PNPM
                ? "<pnpmVersion>${" + versionPropertyName + "}</pnpmVersion>"
                : "<npmVersion>${" + versionPropertyName + "}</npmVersion>";

        mavenPomReaderWriter.updatePluginManagementPlugins(plugins -> {
            plugins.append("""
                                   <plugin>
                                       <groupId>com.github.eirslett</groupId>
                                       <artifactId>frontend-maven-plugin</artifactId>
                                       <version>${frontend-maven-plugin.version}</version>
                                       <executions>
                                           <execution>
                                               <id>install-frontend-tooling</id>
                                               <goals>
                                    <goal>%s</goal>
                                               </goals>
                                               <configuration>
                                                   <nodeVersion>${frontend-maven-plugin.nodeVersion}</nodeVersion>
                                    %s
                                               </configuration>
                                           </execution>
                                           <execution>
                                <id>run-%s-install</id>
                                               <goals>
                                    <goal>%s</goal>
                                               </goals>
                                           </execution>
                                           <execution>
                                <id>run-%s-build</id>
                                               <goals>
                                    <goal>%s</goal>
                                               </goals>
                                               <configuration>
                                                   <arguments>run build</arguments>
                                               </configuration>
                                           </execution>
                                       </executions>
                    </plugin>""".formatted(installGoal, installVersionConfig, runGoal, runGoal, runGoal, runGoal));
        });

        mavenPomReaderWriter.updateBuildPlugins(plugins -> {
            plugins.append("""
                                   <plugin>
                                       <groupId>com.github.eirslett</groupId>
                                       <artifactId>frontend-maven-plugin</artifactId>
                                   </plugin>""");
        });

        if (addReleaseProfile) {
            mavenPomReaderWriter.updateProfiles(profiles -> {
                profiles.append("""
                                        <profile>
                                            <id>release</id>
                                            <build>
                                                <plugins>
                                                    <plugin>
                                                        <groupId>com.github.eirslett</groupId>
                                                        <artifactId>frontend-maven-plugin</artifactId>
                                                        <executions>
                                                            <execution>
                                                <id>run-%s-build</id>
                                                                <goals>
                                                    <goal>%s</goal>
                                                                </goals>
                                                                <configuration>
                                                                    <arguments>run build-prod</arguments>
                                                                </configuration>
                                                            </execution>
                                                        </executions>
                                                    </plugin>
                                                </plugins>
                                            </build>
                        </profile>""".formatted(runGoal, runGoal));
            });
        }
        mavenPomReaderWriter.write();
    }

    public static void updateGitIgnore(Path base) throws IOException {
        Path path = base.resolve(".gitignore");
        Files.createDirectories(path.getParent());
        String s = """
                
                # Excludes for npm/pnpm
                node_modules
                node
                """;
        if (!Files.exists(path)) {
            Files.writeString(path, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(path, s, StandardOpenOption.APPEND);
        }
    }

    public static String pnpmAllowBuildsHelpText() {
        return """
                
                ## pnpm: `allowBuilds`
                
                Your project contains a `pnpm-workspace.yaml` with an `allowBuilds` entry. From pnpm 10, install
                scripts of dependencies (used by native packages such as `@tailwindcss/oxide`, `lightningcss`,
                `esbuild` to fetch their native binaries) are blocked by default for security reasons — see
                https://pnpm.io/settings#allowbuilds for details.
                
                The list pre-populates the packages used by the generated project so the first build runs without
                manual approval. The same list is repeated under `onlyBuiltDependencies`, which is what pnpm 10
                reads; pnpm 11 and later only use `allowBuilds`. You can:
                
                * Add more packages to the list as you introduce dependencies that need build scripts.
                * Remove `onlyBuiltDependencies` if everybody on the project uses pnpm 11 or later.
                * Remove the entries if you prefer pnpm's interactive `pnpm approve-builds` workflow.
                * Audit the list periodically — anything in it can run arbitrary scripts during install.
                """;
    }

    private NpmHelper() {

    }
}
