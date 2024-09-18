# sanatoriocolegiales/lad-webhook

Servicio que consume webhook de LAD para procesamiento de historias clínicas realizadas a través de teleconsulta por guardia.

## Project Status

WIP

Project created with [deps-new](https://github.com/seancorfield/deps-new) and the [practicalli/service template](https://github.com/practicalli/project-templates)

## TODOS

[X] Está pendiente la definición de al menos una tabla que habría que agregar
[X]  Hay que filtrar el tipo de evento
[ ] ~~Faltaría la tabla de tbc_patologia~~
[] Probar integración con testcontainers
[X] Verificar todos los campos que hay que actualizar en tbc_guardia
[X] Implementar middleware para manejo de excepciones 
[X] ~~Implementar autenticación por JWT~~ Autenticación tipo OAUTH2
[] Implementar tests para generación de registros
[X] Implementar la ejecución de un script que cree las bases y tablas necesarias en SQLite
[X] Corregir problema de conexión con SQLite usando Hikari

## Run the service

Run the service (clojure.main)

```shell
clojure -M:run/service
```

## Development

List all the available project tasks using the `make` help

```shell
make
```

> This project uses `make` tasks to run the Clojure tests, kaocha test runner and package the service into an uberjar.  The `Makefile` uses `clojure` commands and arguments that can be used directly if not using `make`.

### Run Clojure REPL

Start the REPL with the [Practicalli REPL Reloaded](https://practical.li/clojure/clojure-cli/repl-reloaded/) aliases to include the custom `user` namespace (`dev/user.clj`) which provides additional tools for development (IntegrantREPL, add-libs hotload, find-deps, Portal data inspector)

```shell
make repl
```

Evaluate `(start)` expression at the repl prompt to start the service.  Several mulog events should be published to the terminal window.

Connect a clojure aware editor and start developing the code, evaluating changes as they are made.

`(refresh)` will reload any changed namespaces

The local nREPL server port will be printed, along with a help menu showing the REPL Reloaded tools available.

> `:dev/reloaded` alias should be included in the editor command (jack-in) to start a REPL


### Live Lint checks

Using an editor with Clojure LSP (or clj-kondo) integration provides diagnostic feedback on syntax and idomatic code use, as that code is typed into the editor and throughout an entire project.

`.lsp/config.edn` provides exclusions where diagnostic warnings add no value, e.g. ignore unused public function warnings where those functions are part of the external interface of the project.


### Unit tests

Run unit tests of the service using the kaocha test runner

```shell
make test
```

> If additional libraries are required to support tests, add them to the `:test/env` alias definition in `deps.edn`

`make test-watch` will run tests on file save, although changes to `template.edn` may require cancelling the test watch (Control-c) and restarting.  test-watch requires Practicalli Clojure CLI Config `:test/watch` alias.

## Format Code

Check the code format before pushing commits to a shared repository, using cljstyle to check the Clojure format, MegaLinter to check format of all other files and kaocha test runner to test the Clojure code.

Before running the `pre-commit-check`

- [install cljstyle](https://github.com/greglook/cljstyle/releases){target=_blank}
- MegaLinter runs in a Docker container, so ensure Docker is running

```shell
make pre-commit-check
```

Run cljstyle only

- `make format-check` runs cljstyle and and prints a report if there are errors
- `make format-fix` updates all files if there are errors (check the changes made via `git diff`)

Run MegaLinter only

- `make lint` runs all configured linters in `.github/config/megalinter.yaml`
- `make lint-fix` as above and applies fixes

Run Kaocha test runner only

- `make test` runs all unit tests in the project, stopping at first failing test
- `make test-watch` detect file changes and run all unit tests in the project, stopping at first failing test


## Deployment

Build an uberjar to deploy the service as a jar file

```shell
make build-uberjar
```

- `make build-config` displays the tools.build configuration
- `make build-clean` deletes the build assets (`target` directory)

```shell
make docker-build
```

- `make docker-down` shuts down all services started with `docker-build`
- `make docker-build-clean`

Or build and run the service via the multi-stage `Dockerfile` configuration as part of a CI workflow.


## License

Copyright © 2024 Jrivero

[Creative Commons Attribution Share-Alike 4.0 International](http://creativecommons.org/licenses/by-sa/4.0/")
