# Taming Thymeleaf CLI

The goal of this project is to provide a command line tool to help set up a Spring Boot project with either JTE or
Thymeleaf as the template engine.

## Usage

1. Download the release from https://github.com/wimdeblauwe/ttcli/releases
2. Unzip the release
3. Run `./ttcli help` (macOS/Linux) or `ttcli.exe help` (Windows) to see the options of the tool.

The application is also available via the following package managers:

* [Homebrew](https://brew.sh/) (Linux/macOS): `brew install wimdeblauwe/homebrew-ttcli/ttcli`
* [Chocolatey](https://chocolatey.org/) (Windows): `choco install ttcli`

### Initialize

Run `ttcli init` to have the tool create a new Spring Boot project with an NPM based live-reload setup.

The tool will ask the following questions:

1. GroupId: the Maven `groupId` that should be used for the project.
2. ArtifactId: the Maven `artifactId` that should be used for the project.
3. Name: the project name
4. Spring Boot version: Select the Spring Boot version to use
5. Template engine: Select either Thymeleaf (default) or JTE as the template engine
6. Live reload setup: Select how you want to have your live reload setup
7. Web dependencies: Select from various CSS and/or JavaScript libraries to be added to your project.

The tool will create a new project in a sub-directory of the current directory with the name of the `artifactId` and apply the following changes:

* Generate a Spring Boot project
* Add NPM dependencies and NPM scripts needed to have a live reload setup
* Add template files as a starting point for your application:
  * For Thymeleaf: `index.html` and `layout/main.html` in the `src/main/resources/templates` directory
  * For JTE: `index.jte` and `layout/main.jte` in the `src/main/jte` directory
* Setup Tailwind CSS when selected.
* Setup Bootstrap when selected.
* Adds webjars to the Maven dependencies.
* Adds an `application-local.properties` with template caching disabled.

> [!NOTE]
> You can choose a different base directory that the tool should use via `ttcli --baseDir <otherdir>`

## Live reload options

The tool allows selecting from various options for having live reload when editing HTML, CSS and JavaScript files.

> [!IMPORTANT]
> Be sure to always read the instructions in the generated HELP.md to know how to work with your live reload selection.

### DevTools

This is the simplest option, but it is also the slowest to reload upon change.
The biggest advantage is that you do not need to have npm installed.

> [!NOTE]
> If you also select 'Tailwind CSS' with this option, then you will need `npm` to make it work.

### Npm based scripts

This uses npm to watch the HTML, CSS and JavaScript files and processes them.
This gives a quicker live reload experience compared to using DevTools.

### Vite

This option uses [Vite](https://vitejs.dev/) for live reloading.
It requires an additional dependency on the Spring Boot side to process the assets and manifest that Vite generates.
The biggest advantages:

* It is very quick to reload the changes.
* Vite supports SASS, TypeScript, ... so you can use all that now in your Spring Boot project with either JTE or
  Thymeleaf.
  If you add the Vite Vue plugin, you can even write Vue components and have live reloading of them working just fine.

## Tailwind CSS support

Support for Tailwind CSS is a bit more involved compared to other web dependency as you can't just add a webjars dependency.
For that reason, the live reload options all do various customizations when you select 'Tailwind CSS' as a web dependency.

## Building

### Regular JVM build

```
mvn verify
```

### Native build

Use GraalVM JDK:

```
sdk use java 22.3.r17-grl
```

Run native compilation:

```
mvn -Pnative native:compile
```

### Releasing

#### Github

An official release is done via
a [manual action on Github Actions](https://github.com/wimdeblauwe/ttcli/actions/workflows/release.yml).

#### Local

> [!NOTE]
> You need to configure
> a [GitHub access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).
> Put it in `~/.jreleaser/config.properties` with for example:
>
> ```properties
> JRELEASER_GITHUB_TOKEN=<github-token-value>
> ```

Check JReleaser configuration:

```
mvn -Djreleaser.dry.run -Prelease jreleaser:config
```

Posting a release from a local environment only when binaries for the current platform are available:

```
mvn -Prelease jreleaser:release -Djreleaser.select.current.platform
```
