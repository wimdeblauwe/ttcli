package io.github.wimdeblauwe.ttcli.livereload.helper;

import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import org.jsoup.nodes.Comment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class NpmHelper {

    public static void updateMavenPom(MavenPomReaderWriter mavenPomReaderWriter,
                                      InstalledApplicationVersions versions,
                                      boolean addReleaseProfile) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\uD83C\uDFFB\u200D♀️ Updating Maven pom.xml");
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
            properties.appendElement("frontend-maven-plugin.version").text("1.15.4");
            properties.appendElement("frontend-maven-plugin.nodeVersion").text(versions.nodeVersion());
            properties.appendElement("frontend-maven-plugin.npmVersion").text(versions.npmVersion());
        });

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
                                                   <goal>install-node-and-npm</goal>
                                               </goals>
                                               <configuration>
                                                   <nodeVersion>${frontend-maven-plugin.nodeVersion}</nodeVersion>
                                                   <npmVersion>${frontend-maven-plugin.npmVersion}</npmVersion>
                                               </configuration>
                                           </execution>
                                           <execution>
                                               <id>run-npm-install</id>
                                               <goals>
                                                   <goal>npm</goal>
                                               </goals>
                                           </execution>
                                           <execution>
                                               <id>run-npm-build</id>
                                               <goals>
                                                   <goal>npm</goal>
                                               </goals>
                                               <configuration>
                                                   <arguments>run build</arguments>
                                               </configuration>
                                           </execution>
                                       </executions>
                                   </plugin>""");
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
                                                                <id>run-npm-build</id>
                                                                <goals>
                                                                    <goal>npm</goal>
                                                                </goals>
                                                                <configuration>
                                                                    <arguments>run build-prod</arguments>
                                                                </configuration>
                                                            </execution>
                                                        </executions>
                                                    </plugin>
                                                </plugins>
                                            </build>
                                        </profile>""");
            });
        }
        mavenPomReaderWriter.write();
    }

    public static void updateGitIgnore(Path base) throws IOException {
        Path path = base.resolve(".gitignore");
        Files.createDirectories(path.getParent());
        String s = """
                
                # Excludes for npm
                node_modules
                node
                """;
        if (!Files.exists(path)) {
            Files.writeString(path, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(path, s, StandardOpenOption.APPEND);
        }
    }

    private NpmHelper() {

    }
}
