# Specification of Liquibase Changesets

## Overview
The database schema for **TrackHub** consists of four main entities:
- `User`
- `Track`
- `Note`
- `TrackPermission`

Each entity will be represented by a dedicated table. All tables will use **PostgreSQL 16** data types and include appropriate constraints, indexes, and foreign keys.

## Required Changesets
| ID | Description | Author | Rollback |
|----|-------------|--------|----------|
| 001 | Create `users` table with columns `id`, `username`, `email`, `password_hash`, `created_at`, `updated_at`. Add primary key, unique constraints on `username` and `email`. | team@trackhub | Drop `users` table |
| 002 | Create `tracks` table with columns `id`, `owner_id`, `title`, `description`, `created_at`, `updated_at`. Add foreign key to `users(id)`. | team@trackhub | Drop `tracks` table |
| 003 | Create `notes` table with columns `id`, `track_id`, `author_id`, `content`, `created_at`, `updated_at`. Add foreign keys to `tracks(id)` and `users(id)`. | team@trackhub | Drop `notes` table |
| 004 | Create `track_permissions` table with columns `id`, `track_id`, `user_id`, `role` (enum: OWNER, EDIT, VIEW). Add foreign keys to `tracks(id)` and `users(id)`. Add unique constraint on `(track_id, user_id)`. | team@trackhub | Drop `track_permissions` table |

## Naming Conventions
- **File name**: `<id>-<description>.xml` (e.g., `001-create-users-table.xml`).
- **Changeset id**: Same as the file id (`001`).
- **Author**: `team@trackhub`.

## Rollback Strategy
Each changeset includes a `<rollback>` element that drops the created table, ensuring full reversibility.
