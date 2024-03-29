# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.12.0]

### Changed

- Verified compatibility with FAIR Data Point 1.12.0

## [1.11.0]

### Changed

- Verified compatibility with FAIR Data Point 1.11.0

## [1.10.0]

### Changed

- Verified compatibility with FAIR Data Point 1.10.0

## [1.9.0]

### Changed

- Verified compatibility with FAIR Data Point 1.9.0

## [1.8.0]

### Changed

- Verified compatibility with FAIR Data Point 1.8.0

## [1.7.0]

### Changed

- Updated to OpenRefine 3.4.1

## [1.6.0]

### Changed

- Verified compatibility with FAIR Data Point 1.6.0

## [1.5.0]

### Added

- Possibility to specify custom storage in frontend
- History of created metadata in new dialog and stored using overlay model
  per project

### Changed

- Saving project when overlay model updated (history)
- Localizable audit table headers
- Adjusted for compatibility with FAIR Data Point 1.5.0

### Fixed

- Virtuoso storage can use HTTP Basic Auth

## [1.4.0]

### Changed

- Verified compatibility with FAIR Data Point 1.4.0

## [1.3.0]

### Added

- Audit log (per-project) with possibility to browse, filter, and clear using 
  a simple dialog in client-side

### Changed

- Prepared getting and passing SHACL specs from FDP to OpenRefine frontend
- Updated for compatibility with newer FAIR Data Point 1.3.0

### Fixed

- Persisting information about stored metadata

## [1.2.0]

### Added

- Possibility to restrict filesize and naming (using patterns) for storage
- Autofill file-related fields with content type and bytesize of stored file using
  "Store FAIR data" button

### Changed

- Updated for compatibility with FAIR Data Point v1.2
- Removed fairmetadata4j

### Fixed

- Clear configuration before reloading it

## [1.1.0]

### Added

- Remembering the last used catalog and dataset per repository (FDP) persistently for the
  OpenRefine project
- Display version and build info of the connected FAIR Data Point if present
- Use `instanceUrl` of FAIR Data Point if present to allow different domains for FDP and for 
  its repository metadata
- Information in About dialog derived from POM and project.properties files
- Support for different OpenRefine version in Docker image using [ARG](https://docs.docker.com/engine/reference/builder/#arg)
- Service tasks endpoint for reloading configuration files (extensible for the future), currently
  intended for testing purposes only (end-to-end tests)

### Changed

- Updated to [OpenRefine 3.3](https://github.com/OpenRefine/OpenRefine/releases/tag/3.3)

### Fixed

- File storing using FTP (file extensions and corrupted binary files)
- Storing metadata in FAIR Data Point (in some cases error occurred)

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

[Unreleased]: /../../compare/master...develop
[1.0.0]: /../../tree/v1.0.0
[1.1.0]: /../../tree/v1.1.0
[1.2.0]: /../../tree/v1.2.0
[1.3.0]: /../../tree/v1.3.0
[1.4.0]: /../../tree/v1.4.0
[1.5.0]: /../../tree/v1.5.0
[1.6.0]: /../../tree/v1.6.0
[1.7.0]: /../../tree/v1.7.0
[1.8.0]: /../../tree/v1.8.0
[1.9.0]: /../../tree/v1.9.0
[1.10.0]: /../../tree/v1.10.0
[1.11.0]: /../../tree/v1.11.0
[1.12.0]: /../../tree/v1.12.0
