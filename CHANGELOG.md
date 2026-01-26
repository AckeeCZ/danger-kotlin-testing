# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
### Added

## [1.0.1] - 2026-01-26
### Changed
- Update Markdown headers for failed unit and snapshot tests

## [1.0.0] - 2026-01-24
### Changed
- Breaking change of `JUnitPlugin` object to `TestingPlugin` object. Also, previous methods `parse` and `report` were
  replaced with `findAndProcessJUnitReports`. On top of previous files parsing and reporting this method now even finds
  all JUnit reports. You can now provide an optional config to modify some functionality if needed.
  See documentation for more details.

### Added
- `TestingPlugin.findAndProcessSnapshotReports` method for processing failed snapshot delta image files and
  reporting them to the pull request. You can provide an optional config to modify some functionality if needed.
  See documentation for more details.
