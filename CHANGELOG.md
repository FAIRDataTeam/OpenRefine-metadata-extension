# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Remembering the last used catalog and dataset per repository (FDP) persistently for the
  OpenRefine project
- Display version and build info of the connected FAIR Data Point if present
- Use `instanceUrl` of FAIR Data Point if present to allow different domains for FDP and for 
  its repository metadata

## [1.0.0]

Initial version based on reproducing functionality of deprecated [FAIRifier](https://github.com/FAIRDataTeam/FAIRifier).

### Added

- Create metadata dialog that handles the connection with a FAIR Data Point and allows 
  metadata selection and creation with a possibility to predefine connections in the 
  configuration
- Metadata forms to create metadata in the selected FAIR Data Point, with typehints and 
  ability to predefine value in the configuration
- Store data dialog that allows to export project in the selected format (using OpenRefine's 
  exported) and send it to the selected storage from those which are configured - currently 
  supports: FTP, Virtuoso, and Triple Stores with HTTP API
- About dialog with basic information about the extension and its compatibility
- Report a bug link to create a GitHub issue easily

[Unreleased]: /../../compare/v1.0.0...develop
[1.0.0]: /../../tree/v1.0.0
