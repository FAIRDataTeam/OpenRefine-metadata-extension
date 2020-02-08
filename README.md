# OpenRefine-metadata-extension

[![Build Status](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension.svg?branch=master)](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension)
![GitHub All Releases](https://img.shields.io/github/downloads/FAIRDataTeam/OpenRefine-metadata-extension/total)
[![Docker Pulls](https://img.shields.io/docker/pulls/fairdata/openrefine-metadata-extension)](https://hub.docker.com/r/fairdata/openrefine-metadata-extension)
[![License](https://img.shields.io/github/license/FAIRDataTeam/OpenRefine-metadata-extension)](LICENSE)
[![Documentation](https://readthedocs.org/projects/fairdatapoint/badge/?version=latest)](https://fairdatapoint.readthedocs.io/en/latest/openrefine/usage.html)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/aca649b193144fb68428ba3039a49ad5)](https://www.codacy.com/manual/MarekSuchanek/OpenRefine-metadata-extension?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FAIRDataTeam/OpenRefine-metadata-extension&amp;utm_campaign=Badge_Grade)

[OpenRefine](http://openrefine.org) extension to support **FAIR Metadata** with use of [FAIR Data Point](https://github.com/FAIRDataTeam/FAIRDataPoint)

## Installation

Distribution ZIP/TGZ files are located at [releases](https://github.com/FAIRDataTeam/OpenRefine-metadata-extension/releases) page.

Just download it and unzip/untar into the `extensions` folder in your OpenRefine instance, for more information visit [OpenRefine - Installing Extensions](https://github.com/OpenRefine/OpenRefine/wiki/Installing-Extensions).

```console
$ unzip metadata-X.Y.Z-OpenRefine-3.3.zip path/to/openrefine-3.3/webapp/extensions
# or
$ tar xzvf metadata-X.Y.Z-OpenRefine-3.3.tgz -C path/to/openrefine-3.3/webapp/extensions
```

### Via Docker

We publish [Docker images](https://hub.docker.com/r/fairdata/openrefine-metadata-extension) that contains OpenRefine 3.3 together with the **metadata** extension. Only requirement is having Docker service running:

```console
$ docker run -p 3333:3333 fairdata/openrefine-metadata-extension
```

Then just open [localhost:3333](http://localhost:3333) in your favorite web browser. Optionally you can change the port binding or run it "detached". Visit Docker `run` [documentation](https://docs.docker.com/engine/reference/run/) for more information.

To persist data and eventually be able to share them across multiple instances of OpenRefine, you need to mount `/data` directory, for example:

```console
$ docker run -p 3333:3333 -v /home/me/openrefine-data:/data:z fairdata/openrefine-metadata-extension
```

To add other extensions (e.g. [RDF extension](https://github.com/stkenny/grefine-rdf-extension)), you can just put them into the mounted folder according to the official [documentation](https://github.com/OpenRefine/OpenRefine/wiki/Installing-Extensions). Always also check installation instruction of the desired extension. For the previous example, you should place your extensions to the directory `/home/me/openrefine-data/extensions`.

### Configuration

Configuration files are located in `extensions/metadata/module/config` folder and examples are provided. In case of Dockerized instance, you need to mount this folder with your configuration files as volume similarly to `data` folder.

For more information, visit [our documentation](https://fairdatapoint.readthedocs.io/en/latest/openrefine/setup.html#configuration).

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

We maintain a [CHANGELOG](CHANGELOG.md), you should also take a look at our [Contributing guidelines](CONTRIBUTING.md) and
[Code of Conduct](CODE_OF_CONDUCT.md).

### Build with Docker

You can also use provided [multistage](https://docs.docker.com/develop/develop-images/multistage-build/) `Dockerfile`
that builds the extension and then runs it in OpenRefine. Therefore, you don't need
any Java dependencies locally, just Docker:

```console
$ docker build -t openrefine-metadata:local-tag .
$ docker run -p 3333:3333 openrefine-metadata:local-tag
```

Then open [http://localhost:3333](http://localhost:3333) in your favorite web browser.

You can also build a Docker image with specific OpenRefine version using argument (see [Dockerfile](Dockerfile) for details):

```console
docker build  . -t  openrefine-metadata:local-or3.2 --build-arg OPENREFINE_VERSION=3.2
```

Note that we are using Java SE 8 (LTS), concretely OpenJDK 8 in Docker images (to comply with OpenRefine requirements).

## License

This project is licensed under MIT license - see the [LICENSE](LICENSE) file for more information.
