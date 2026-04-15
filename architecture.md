# Architecture of Database Migrations (Liquibase)

## Overview
The project will use **Liquibase 4.x** to manage database schema changes for the PostgreSQL 16 database. All migration files will be placed under the resources directory:
```
src/main/resources/db/changelog/
```
The directory will contain:
- `db.changelog-master.xml` – the master changelog that includes all individual changeset files.
- Individual changelog files (e.g., `001-create-user-table.xml`, `002-create-track-table.xml`, ...). Each file is **atomic** and contains a single logical change.

## Integration with Maven
Liquibase will be executed via the `liquibase-maven-plugin` defined in `pom.xml`. The plugin will be bound to the `process-resources` phase for generating SQL scripts and can be invoked manually for `update`, `rollback`, `status`, etc.

## Naming Conventions
- **File name**: `<sequence>-<description>.xml` where `<sequence>` is a zero‑padded incremental number (e.g., `001`, `002`).
- **Changeset id**: Same as the file sequence number.
- **Author**: Project team identifier, e.g., `team@trackhub`.

## Rollback Strategy
Each changeset must include a `<rollback>` element that reverses the operation (e.g., `dropTable`, `dropColumn`). This ensures that any migration can be undone safely.

## Version Control
All changelog files are version‑controlled alongside the source code. The master file is updated to include new changelogs using the `<include>` tag.

## Execution Workflow
1. **Develop**: Add a new changelog file with the required changeset.
2. **Commit**: Include the changelog file and update `db.changelog-master.xml`.
3. **CI/CD**: During the build, the Maven plugin can run `liquibase:update` against a test database to verify migrations.
4. **Production**: Run `liquibase:update` on the production database.

## Example Directory Structure
```
src/main/resources/
├─ application.properties
└─ db/changelog/
   ├─ db.changelog-master.xml
   ├─ 001-create-user-table.xml
   ├─ 002-create-track-table.xml
   └─ 003-create-note-table.xml
```
