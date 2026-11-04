# java.nio.file.LinkPermission 设计文档

## 1. API 功能

用于控制「创建硬链接/符号链接」操作的权限，final 类，继承 `BasicPermission`（已 `@Deprecated(forRemoval)`）。

- 构造器：`LinkPermission(String name)`（name 必须为 `"hard"` 或 `"symbolic"`）、
  `LinkPermission(String name, String actions)`（actions 必须为空串或 null）。
- 查询：`getName()`、`getActions()`、`equals/hashCode`、`implies(Permission)`（继承自 `BasicPermission`）。
- 校验：非法 name（非 hard/symbolic）或非空 actions 抛 `IllegalArgumentException`。

## 2. Java 用户如何使用

- 组合链路：`new LinkPermission("hard")` → `getName()`/`getActions()` → `implies(new LinkPermission("hard"))`。
- 边界：`new LinkPermission("hard","")` 与 `new LinkPermission("symbolic", null)` 合法；`"foo"` 或 `"hard","read"` 非法。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/file/LinkPermission.java`）：
  `checkName` 只允许 `"hard"`/`"symbolic"`；两参构造器拒绝非空 actions；类已标记 `@Deprecated(since="25", forRemoval=true)`。

## 4. 测试用例设计 + 自动化实现

文件：`LinkPermissionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `BasicPermission` / 两个构造器。
- 黑盒：name/actions 读回 → `equals/hashCode` → `implies` → 两参构造器空/ null actions →
  非法 name/actions 异常。

### 第 4 部分追加（批次 A10 契约深化）

新增：计算型 serialVersionUID=-1441492453772213220（与 JDK-8 同值实测）；序列化 round-trip；newPermissionCollection 非 null + implies 链；implies(null) false、跨类 equals false、toString 含名；负例扩展（basic/空名/双参空名 IAE、null 名 NPE）。注：JDK-27 该类已 @deprecated(forRemoval)。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
