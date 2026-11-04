# java.lang.reflect.ReflectPermission — 测试设计案（JDK-8）

## 1. API 功能

- `final class ReflectPermission extends BasicPermission`：
  - 构造器 `(String name)`、`(String name, String actions)`；actions 被完全忽略（getActions 恒为 ""，null 也归一为 ""）。
  - 语义全部继承 BasicPermission：`*` 全通配、尾缀 `.*` 层级通配、`implies/equals/hashCode/newPermissionCollection`。
  - 已知合法名：`suppressAccessChecks`、`newProxyInPackage.<pkg>`。

## 2. 用户如何使用

- SecurityManager 在位时（策略文件/`AccessController.checkPermission`）授予反射相关能力；无 SM 的普通业务代码不触碰。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/ReflectPermission.html：目标名清单与继承说明）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/ReflectPermission.java`、`java/security/BasicPermission.java`（名字校验与 implies 实现）。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/ReflectPermission/`。

## 4. 测试用例设计（映射到 ReflectPermissionTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 基本名称/actions/equals（保留原有） | testReflectPermission |
| P0 | 层次普查：BasicPermission/Permission/Guard/Serializable、final、2 构造器 | testHierarchyCensus |
| P0 | actions 忽略：双参（含 null actions）getActions==""、equals/hashCode/implies 与单参等价 | testActionsIgnored |
| P0 | implies 格：自反、`*` 蕴含特定、特定不蕴含 `*`、异名、`.*` 层级、兄弟包、深层不蕴含浅层、异型 Permission | testImpliesLattice |
| P0 | 名字校验：null → NPE "name can't be null"、空串 → IAE "name can't be empty"（单/双参一致） | testNameValidation |
| P1 | 名中 `*`/`?` 被当普通字符接受（8/27 一致）、`*` 仍蕴含之 | testNameValidation |
| P1 | equals 异型/null、RuntimePermission 同名不等不蕴含、toString 含名 | testEqualityForeignTypes |
| P1 | PermissionCollection：add 后蕴含、未加不蕴含、通配覆盖、枚举非空 | testPermissionCollection |
| P1 | 序列化 round-trip：类型/名称/空 actions/equals/hashCode/implies 对称 | testSerialization |

## 易错规范细节（已锁入断言）

- null 名 → NPE（消息 "name can't be null"）、空串名 → IAE（"name can't be empty"）；JDK-8 与 JDK-27 措辞一致（取片段断言）。
- 中间含 `*`/`?` 的名字不报错，按普通名处理（非 BasicPermission 拒绝项）；只有 null/空串被拒。
- getActions 恒 ""（BasicPermission 归一），equals/hashCode/implies 均忽略 actions。
- ReflectPermission 永不蕴含其它 Permission 子类实例（含同名 RuntimePermission），反之亦然。
