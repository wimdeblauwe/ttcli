# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`ttcli` is a Spring Boot + Spring Shell command-line tool that scaffolds a new Spring Boot project pre-wired with
Thymeleaf or JTE, a chosen live-reload setup, and selected web dependencies. It is distributed both as a runnable JAR
and as a GraalVM native image (Homebrew, Chocolatey, release zips).

Java 25 is required (see `.sdkmanrc` — `java=25.0.1-graal`). The project is on Spring Boot 4 / Spring Shell 4.

## Common commands

- Build + run tests: `./mvnw verify`
- Run a single test: `./mvnw test -Dtest=FileUtilTest` or `./mvnw test -Dtest=FileUtilTest#methodName`
- Run the CLI from source: Run `mvn package`, afterward run `java -jar target/ttcli-<version>.jar init`. If there is
  already a `demo` directory at the root of the project, remove it first.
- Native image build: switch to GraalVM JDK (`sdk use java 25.0.1-graal`), then `./mvnw -Pnative native:compile`

`SpringBootInitializrClientManualTest` reaches `start.spring.io` and is intentionally a manual test; the rest of
`src/test` is offline unit tests.

## Architecture

The single user-facing command is `Init` (`io.github.wimdeblauwe.ttcli.Init`), exposed via Spring Shell's `@Command`. It
drives an interactive `ComponentFlow` to collect answers, then delegates everything to `ProjectInitializationService`,
which orchestrates the generation in a fixed order:

1. `SpringBootInitializrService` — calls start.spring.io to fetch a base project (metadata via
   `SpringBootInitializrClient` / `InitializrMetadata`).
2. `JavaCodeInitService` — writes any project Java code.
3. The chosen `LiveReloadInitService` — generates its setup (config files, scripts, dependencies) and contributes extra
   Initializr deps.
4. `HelpTextInitService` — writes `HELP.md` (each live-reload strategy supplies its own help text).
5. `MavenInitService` — patches the generated `pom.xml` (uses `jsoup`/`xsoup` for HTML/XML manipulation).
6. `TailwindDependencyInitService` — applies selected Tailwind plugins (forms, typography, daisyUI, container-queries).
7. Template engine generator: `ThymeleafTemplatesInitService` *or* `JteTemplatesInitService` depending on
   `TemplateEngineType`.
8. `liveReloadInitService.runBuild(...)` — runs the initial build (e.g. `npm install`) inside the generated project.

### Live-reload strategy pattern

`LiveReloadInitService` has three families of implementations under `livereload/`: `devtools`, `npm`, `vite`. Each
family has a plain variant plus Tailwind-specialized variants (Tailwind 3 and Tailwind 4) that implement
`TailwindCssSpecializedLiveReloadInitService`. `LiveReloadInitServiceFactory` keeps the "normal" services (shown in the
picker) separate from the Tailwind-specialized ones, and swaps the selected service for its Tailwind-specialized
counterpart when the user picked Tailwind CSS as a web dependency. When adding a new live-reload mode, add the plain
implementation **and** Tailwind 3 + Tailwind 4 specializations so the swap can always resolve.

### Web dependencies and Tailwind plugins

`deps/WebDependency` is a Spring-bean strategy — each implementation (Alpine, Bootstrap, Htmx, Shoelace, TailwindCss)
advertises an id/displayName and contributes its install logic. Most are webjars-based (`WebjarsBasedWebDependency`);
Tailwind CSS is the special case that interacts with the live-reload strategy. Selecting Tailwind opens additional
prompts driven by `tailwind/TailwindDependency` beans and `TailwindVersion` (V3 vs V4). To add a new web dependency or
Tailwind plugin, register it as a Spring component implementing the relevant interface — `Init` autowires the full
`List<WebDependency>` / `List<TailwindDependency>`.

### Generated-project assets

Templates and scaffolding files that are copied into generated projects live under `src/main/resources/files/` (
`templates/thymeleaf`, `templates/jte`, `livereload/npm/*`, …). Code that writes these files reads them from the
classpath — keep these resources in sync when changing live-reload variants.

### Native image

`TamingThymeleafCliApplicationRuntimeHints` registers reflection / resource hints for the native build. Anything
touching new classpath resources, reflection, or dynamic proxying needs a matching hint here, or the `native` profile
build will succeed but the binary will fail at runtime.

## Releasing

Releases are normally cut from GitHub Actions (`.github/workflows/release.yml`). A local release needs
`JRELEASER_GITHUB_TOKEN` in `~/.jreleaser/config.properties` and is run via
`./mvnw -Prelease jreleaser:release -Djreleaser.select.current.platform`. JReleaser also publishes a Homebrew formula (
templated under `src/main/jreleaser/distributions/brew`) and a Chocolatey package.
