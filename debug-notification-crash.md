# Debug Session: notification-crash

**Status:** `[OPEN]`

**Symptom:** 在MuMu模拟器中，点击通知图标后App闪退，无法进入通知页面。

**Environment:** Android App (CampusHub), MuMu Emulator

---

## Hypotheses

1. **Room Schema Mismatch** — 新增了 `NotificationEntity` 表，但 `AppDatabase` 版本号未递增，Room检测到schema变化但版本未变，抛出 `IllegalStateException` 导致崩溃。
2. **SwipeToDismissBox API Incompatibility** — `NotificationScreen` 中使用的 `SwipeToDismissBox` 可能不在当前项目依赖的 Compose Material3 版本中，调用时触发 `NoSuchMethodError`。
3. **Navigation Parameter Mismatch** — `ExploreScreen` 跳转到 `NotificationScreen` 时路由或参数传递有误，导致导航层异常。
4. **Flow/State Initial Value Crash** — `unreadCount` 或 `uiState` 的初始状态在重组时触发了空指针或未处理的异常。
5. **Missing Import/Compose Annotation** — 虽然编译通过，但可能存在运行时Compose内部状态不一致的问题。

---

## Evidence Log

**Static Analysis Finding:**
- `AppDatabase` version was still `1` after adding `NotificationEntity` to entities list.
- Room performs strict schema validation when version hasn't changed. Existing database (with only 2 tables) doesn't match expected schema (3 tables), causing `IllegalStateException` on startup.
- `fallbackToDestructiveMigration()` only triggers on version change, not on schema mismatch with same version.

**Fix Applied:**
- Bumped `AppDatabase` version from `1` → `2`. This triggers `fallbackToDestructiveMigration()`, which destroys and recreates the database with the new schema.

⚠️ Note: This will clear existing user data (registered activities & favorites) since we use destructive migration. For a production app, a proper `Migration(1, 2)` should be written instead.

