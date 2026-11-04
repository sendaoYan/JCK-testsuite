# javax.sound.sampled.AudioPermission — DESIGN

## 1. API 功能
`AudioPermission` 表示对音频系统资源的访问权限，继承 `java.security.BasicPermission`，只有目标名、无动作列表。方法分组：
- 构造器：`AudioPermission(String name)`、`AudioPermission(String name, String actions)`（actions 未使用，应为 null）。
- 继承自 `BasicPermission` 的 `getName()`、`getActions()`、`equals/hashCode`、`implies(Permission)`、`toString()`。

## 2. Java 用户如何使用
用户 `new AudioPermission("play")` / `new AudioPermission("record")` 表示播放/录制权限，用 `implies` 判断一个权限是否蕴含另一个（支持 `*` 通配与 `play.*` 层级通配）。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioPermission.java:67`：单参构造器 `super(name)`；`AudioPermission.java:81` 双参构造器 `super(name, actions)`。
- `src/java.base/share/classes/java/security/BasicPermission.java:87`：`init` 解析 `*` 与 `.*` 通配；`implies` 中 `*` 蕴含任意非通配权限，`a.*` 蕴含 `a.b` 但不蕴含 `a`。

## 4. 测试用例设计 + 自动化实现（`AudioPermissionTest.testBlackBox`）
- 名字往返：单参/双参构造器 `getName()`，`getActions()` 为空串。
- `equals/hashCode` 按名字；与不同名/null 不等。
- `implies`：`*` 蕴含 `play/record`，`play` 蕴含自身、不蕴含 `record`，`play.*` 蕴含 `play.record` 但不蕴含 `play`。
- `toString()` 含名字。
