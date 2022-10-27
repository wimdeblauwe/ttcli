package io.github.wimdeblauwe.ttcli;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MavenPomReaderWriter {
    private final Path pomPath;
    private final Document document;

    private MavenPomReaderWriter(Path pomPath,
                                 Document document) {
        this.pomPath = pomPath;
        this.document = document;
    }

    public String getProjectArtifactId() {
        return Xsoup.compile("/project/artifactId").evaluate(document).get();
    }

    public static MavenPomReaderWriter readFrom(Path base) throws IOException {
        Path pomPath = base.resolve("pom.xml");
        Document document = Jsoup.parse(pomPath.toFile(), StandardCharsets.UTF_8.name(), "", Parser.xmlParser());
        return new MavenPomReaderWriter(pomPath, document);
    }

    public void updateProperties(Consumer<Element> properties) {
        Element project = getProject();
        Element propertiesEl = project.getElementsByTag("properties").get(0);
        properties.accept(propertiesEl);
    }

    public void updateResources(Consumer<Element> resources) {
        Element project = getProject();
        Element build = getBuild(project);
        Element resourcesEl = build.prependElement("resources");
        resources.accept(resourcesEl);
    }

    public void updatePluginManagementPlugins(Consumer<Element> plugins) {
        Element build = getBuild(getProject());
        Element pluginsEl = build.appendElement("pluginManagement")
                                 .appendElement("plugins");
        plugins.accept(pluginsEl);
    }

    public void updateBuildPlugins(Consumer<Element> plugins) {
        Element build = getBuild(getProject());
        Element pluginsEl = build.getElementsByTag("plugins").first();
        plugins.accept(pluginsEl);
    }

    public void updateProfiles(Consumer<Element> profiles) {
        Element profilesEl = getProject().appendElement("profiles");
        profiles.accept(profilesEl);
    }

    public void write() throws IOException {
        Files.writeString(pomPath, document.outerHtml());
    }

    private Element getProject() {
        return document.getElementsByTag("project").first();
    }

    private static Element getBuild(Element project) {
        return project.getElementsByTag("build").first();
    }
}
