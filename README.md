# OpenRefine-metadata-extension

[![Build Status](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension.svg?branch=master)](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension)
![GitHub All Releases](https://img.shields.io/github/downloads/FAIRDataTeam/OpenRefine-metadata-extension/total)
[![Docker Pulls](https://img.shields.io/docker/pulls/fairdata/openrefine-metadata-extension)](https://hub.docker.com/r/fairdata/openrefine-metadata-extension)
[![License](https://img.shields.io/github/license/FAIRDataTeam/OpenRefine-metadata-extension)](LICENSE)

[OpenRefine](http://openrefine.org) extension to support **FAIR Metadata**

## Installation

Distribution ZIP/TGZ files are located at [releases](https://github.com/FAIRDataTeam/OpenRefine-metadata-extension/releases) page.

Just download it and unzip/untar into the `extensions` folder in your OpenRefine instance, for more information visit [OpenRefine - Installing Extensions](https://github.com/OpenRefine/OpenRefine/wiki/Installing-Extensions).

```console
$ unzip metadata-X.Y.Z-OpenRefine-3.2.zip path/to/openrefine-3.2/webapp/extensions
# or
$ tar xzvf metadata-X.Y.Z-OpenRefine-3.2.tgz -C path/to/openrefine-3.2/webapp/extensions
```

### Via Docker

We publish [Docker images](https://hub.docker.com/r/fairdata/openrefine-metadata-extension) that contains OpenRefine 3.2 together with the **metadata** extension. Only requirement is having Docker service running:

```console
$ docker pull fairdata/openrefine-metadata-extension
$ docker run -p 3333:3333 fairdata/openrefine-metadata-extension
```

Then just open [localhost:3333](http://localhost:3333) in your favorite web browser. Optionally you can change the port binding or run it "detached". Visit Docker `run` [documentation](https://docs.docker.com/engine/reference/run/) for more information.

## Development

You are required to have Maven installed with other necessary tools for building Java (see [OpenRefine - Documentation for Developers](https://github.com/OpenRefine/OpenRefine/wiki/Documentation-For-Developers)):

```console
$ mvn clean compile
```

To create a distribution ZIP/TGZ files, run:

```console
$ mvn package
```

After issuing these commands, prepared ZIP/TGZ files should be located in `target` directory according to the Maven output.

### Build with Docker

You can also use provided [multistage](https://docs.docker.com/develop/develop-images/multistage-build/) `Dockerfile`
that builds the extension and then runs it in OpenRefine. Therefore, you don't need
any Java dependencies locally, just Docker:

```console
$ docker build -t openrefine-metadata .
$ docker run -p 3333:3333 openrefine-metadata
```

Then open [http://localhost:3333](http://localhost:3333) in your favorite web browser.

Note that we are using Java SE 11 (LTS), concretely OpenJDK 11 in Docker images.

## License

This project is licensed under MIT license - see the [LICENSE](LICENSE) file for more information.
