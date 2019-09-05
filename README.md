# OpenRefine-metadata-extension

[![Build Status](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension.svg?branch=master)](https://travis-ci.com/FAIRDataTeam/OpenRefine-metadata-extension)
![GitHub All Releases](https://img.shields.io/github/downloads/FAIRDataTeam/OpenRefine-metadata-extension/total)
[![License](https://img.shields.io/github/license/FAIRDataTeam/OpenRefine-metadata-extension)](LICENSE)

[OpenRefine](http://openrefine.org) extension to support **FAIR Metadata**

## Installation

Distribution ZIP files are located at [releases](https://github.com/FAIRDataTeam/OpenRefine-metadata-extension/releases) page. 

Just download it and unzip into the `extensions` folder in your OpenRefine instance, for more information visit [OpenRefine - Installing Extensions](https://github.com/OpenRefine/OpenRefine/wiki/Installing-Extensions).

## Development

You are required to have Maven installed with other necessary tools for building Java (see [OpenRefine - Documentation for Developers](https://github.com/OpenRefine/OpenRefine/wiki/Documentation-For-Developers)):

```console
$ mvn clean compile
```

To create a distribution ZIP file, run:

```console
$ mvn package assembly:single
```

After issuing these commands, prepared ZIP file should be located in `target` directory according to Maven output.

## License

This project is licensed under MIT license - see the [LICENSE](LICENSE) file for more information.
