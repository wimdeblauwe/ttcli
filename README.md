# Taming Thymeleaf CLI

> 📕 Want to build a full web application with Spring Boot and Thymeleaf?
> Read [Taming Thymeleaf](https://www.wimdeblauwe.com/books/taming-thymeleaf/).

The goal of this project is to provide a command line tool to help set up a Spring Boot project with either JTE or
Thymeleaf as the template engine.

## Usage

1. Download the release from https://github.com/wimdeblauwe/ttcli/releases
2. Unzip the release
3. Run `./ttcli help` (macOS/Linux) or `ttcli.exe help` (Windows) to see the options of the tool.

The application is also available via the following package managers:

* [Homebrew](https://brew.sh/) (Linux/macOS): `brew install wimdeblauwe/homebrew-ttcli/ttcli`
* [Chocolatey](https://chocolatey.org/) (Windows): `choco install ttcli`

You can also run it via [JBang](https://www.jbang.dev/):

```
jbang run ttcli@wimdeblauwe init
```

### Initialize

Run `ttcli init` to create a new Spring Boot project. The tool asks a series of questions and then generates the project
in a sub-directory of the current directory, named after the `artifactId`.

> [!NOTE]
> You can choose a different base directory that the tool should use via `ttcli init --baseDir <otherdir>`

> [!NOTE]
> The tool needs internet access to reach [start.spring.io](https://start.spring.io), which generates the base project.

### Questions

The questions below are asked in this order. Some are only asked when they apply to an earlier answer.

| Question                       | Options                                                                    | Default          | Asked when                                                                    |
|--------------------------------|----------------------------------------------------------------------------|------------------|-------------------------------------------------------------------------------|
| Group                          | Any Maven `groupId`                                                        | `com.example`    | Always                                                                        |
| Artifact                       | Any Maven `artifactId`                                                     | `demo`           | Always                                                                        |
| Project Name                   | Any name                                                                   | `Demo`           | Always                                                                        |
| Spring Boot version            | The versions currently offered by start.spring.io                          | start.spring.io default | Always                                                                 |
| Java version                   | The Java versions currently offered by start.spring.io                     | start.spring.io default | Always                                                                 |
| Template engine                | Thymeleaf, JTE                                                             | Thymeleaf        | Always                                                                        |
| Use Thymeleaf Layout Dialect?  | Yes, No                                                                    | Yes              | Thymeleaf is selected                                                         |
| Live reload implementation     | DevTools based, NPM based, Vite                                            | –                | Always. With JTE, only Vite is available.                                     |
| Package manager                | npm, pnpm                                                                  | npm              | NPM based or Vite is selected, or DevTools based together with Tailwind CSS   |
| Web dependencies (multi-select)| Alpine.js, Bootstrap, Htmx, Shadleaf, Shoelace, Tailwind CSS               | none             | Always. Shadleaf is only offered with Thymeleaf and Spring Boot 4.1 or newer. |
| htmx version                   | htmx 2, htmx 4                                                             | htmx 4           | Htmx is selected and either JTE or Spring Boot 4.x is used (otherwise htmx 2) |
| Tailwind version               | Tailwind 3, Tailwind 4                                                     | Tailwind 4       | Tailwind CSS is selected without Shadleaf (Shadleaf always uses Tailwind 4)   |
| Tailwind dependencies (multi-select) | @tailwindcss/forms, @tailwindcss/typography, @tailwindcss/container-queries, daisyUI | none | Tailwind CSS is selected                                  |

### What gets generated

* A Spring Boot project from start.spring.io with the `web` dependency, plus `thymeleaf` or `jte` depending on the
  template engine (and `devtools` for the DevTools based live reload).
* A `HomeController` and starter templates:
  * For Thymeleaf: `index.html` and `layout/main.html` in the `src/main/resources/templates` directory
  * For JTE: `index.jte` and `layout/main.jte` in the `src/main/jte` directory
* The `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect` dependency, when you chose to use the Thymeleaf Layout Dialect.
* The configuration for the selected live reload implementation (see [Live reload options](#live-reload-options)),
  including a `frontend-maven-plugin` setup in the `pom.xml` when npm or pnpm is used, so `mvn package` also builds the
  frontend assets.
* For the NPM based and Vite live reload: an `application-local.properties` with template caching disabled, used with
  the `local` Spring profile.
* The selected web dependencies (see [Web dependencies](#web-dependencies)).
* A `HELP.md` that explains how to start the live reload setup you selected.

## Live reload options

The tool allows selecting from various options for having live reload when editing HTML, CSS and JavaScript files.

> [!IMPORTANT]
> Be sure to always read the instructions in the generated HELP.md to know how to work with your live reload selection.

| Option         | Thymeleaf | JTE | Needs npm/pnpm         |
|----------------|-----------|-----|------------------------|
| Vite           | ✅        | ✅  | Yes                    |
| NPM based      | ✅        | ❌  | Yes                    |
| DevTools based | ✅        | ❌  | Only with Tailwind CSS |

> [!TIP]
> If you don't know which option to choose, select Vite. It is the fastest and most flexible option.

### DevTools

This is the simplest option, but it is also the slowest to reload upon change.
The biggest advantage is that you do not need to have npm installed.

To use it:

1. Install the LiveReload browser extension in your browser.
2. Configure your IDE to compile automatically on save. For IntelliJ, enable 'Build project automatically' and
   'Allow auto-make to start even if developed application is currently running'.
3. Run the Spring Boot application and open http://localhost:8080 with the LiveReload extension active.

> [!NOTE]
> If you also select 'Tailwind CSS' with this option, then you will need `npm` or `pnpm` to make it work.
> The Tailwind setup is generated in `src/main/frontend`. Run `npm run watch` (or `pnpm run watch`) in that directory
> to regenerate the CSS while you work.

### Npm based scripts

This uses npm (or pnpm) to watch the HTML, CSS and JavaScript files and processes them.
This gives a quicker live reload experience compared to using DevTools.
It uses PostCSS for CSS, Babel for JavaScript and Browsersync to reload the browser.

To use it:

1. Run the Spring Boot application with the `local` profile.
2. Run `npm run build && npm run watch` (or the `pnpm` equivalent).
3. Your default browser opens at http://localhost:3000 (Browsersync proxies the application on port 8080).

> [!NOTE]
> If you use a separate authentication server (e.g. social logins or Keycloak), you might get redirected to
> http://localhost:8080 after login. Switch back to port `3000` to keep live reload working.

### Vite

This option uses [Vite](https://vitejs.dev/) for live reloading.
It requires an additional dependency on the Spring Boot side to process the assets and manifest that Vite generates.
The tool adds `io.github.wimdeblauwe:vite-spring-boot-thymeleaf` or `io.github.wimdeblauwe:vite-spring-boot-jte` for
this, and creates a `vite.config.js` that uses the `@wim.deblauwe/vite-plugin-spring-boot` plugin.

The biggest advantages:

* It is very quick to reload the changes.
* Vite supports SASS, TypeScript, ... so you can use all that now in your Spring Boot project with either JTE or
  Thymeleaf.
  If you add the Vite Vue plugin, you can even write Vue components and have live reloading of them working just fine.

To use it:

1. Start the Vite development server with `npm run dev` (or `pnpm run dev`).
2. Run the Spring Boot application with the `local` profile, for example
   `mvn spring-boot:run -Dspring-boot.run.profiles=local`.
3. Open http://localhost:8080.

## Package manager support

It is possible to use NPM or PNPM as the package manager for the live reload setup.
The selected package manager must be installed, together with Node.js, before running `ttcli init`.
The generated `HELP.md` and `package.json` scripts use the package manager you selected.

## Web dependencies

| Web dependency | How it is added                                                                                                         |
|----------------|-------------------------------------------------------------------------------------------------------------------------|
| Alpine.js      | WebJar `org.webjars.npm:alpinejs`, script linked in the layout template                                                 |
| Bootstrap      | WebJar `org.webjars.npm:bootstrap`, CSS and script linked in the layout template                                        |
| Htmx           | WebJar `org.webjars.npm:htmx.org` (htmx 2 or 4). With Thymeleaf, also `io.github.wimdeblauwe:htmx-spring-boot-thymeleaf` |
| Shadleaf       | `io.github.wimdeblauwe:shadleaf-spring-boot-starter`, `sl/layout` fragments, theme toggle and example button in the layout template |
| Shoelace       | WebJar `org.webjars.npm:shoelace-style__shoelace`, light theme CSS and autoloader script linked in the layout template  |
| Tailwind CSS   | Installed via npm/pnpm and wired into the selected live reload setup (see below)                                         |

The tool always adds `org.webjars:webjars-locator-lite` (Spring Boot 3.4 and newer) or `org.webjars:webjars-locator`
(older Spring Boot versions), so WebJar URLs work without a version number.

### Shadleaf

[Shadleaf](https://shadleaf.dev) is only offered with Thymeleaf and Spring Boot 4.1 or newer. It works together with
other web dependencies:

* Shadleaf ships its own Alpine.js. If you also select Alpine.js, the Alpine.js WebJar is not added, and you can use
  Alpine in your own markup through Shadleaf's copy.
* If you also select Tailwind CSS, Tailwind 4 is used without asking. `shadleaf.assets.variant=embedded` is set in
  `application.properties`, and `application.css` maps Shadleaf's tokens onto Tailwind utilities (e.g. `bg-primary`).

### htmx versions

htmx 4 with Thymeleaf requires `htmx-spring-boot-thymeleaf` 6.x, which needs Spring Boot 4. When you pick an older
Spring Boot version with Thymeleaf, the tool uses htmx 2 without asking.

## Tailwind CSS support

Support for Tailwind CSS is a bit more involved compared to other web dependency as you can't just add a webjars dependency.
For that reason, the live reload options all do various customizations when you select 'Tailwind CSS' as a web dependency.

You can choose between Tailwind 3 and Tailwind 4 (the default). You can also add these Tailwind plugins:

* `@tailwindcss/forms`
* `@tailwindcss/typography`
* `@tailwindcss/container-queries`
* `daisyUI` (`daisyui@4` with Tailwind 3, `daisyui@beta` with Tailwind 4)

## Building

### Regular JVM build

```
mvn verify
```

### Native build

Use GraalVM JDK:

```
sdk use java 25.0.1-graal
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
