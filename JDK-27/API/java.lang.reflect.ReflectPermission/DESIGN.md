# java.lang.reflect.ReflectPermission — 测试设计（JDK-27）

## 1. API 功能
`ReflectPermission extends BasicPermission`（JDK-27 已标记 deprecated for removal，随 SecurityManager 体系退场）：
- 构造器：`(String name)`、`(String name, String actions)`（actions 被忽略）。
- 语义沿 `BasicPermission`：无 actions、名字支持 `*`/尾缀 `.*` 层级通配、`implies/equals/hashCode/newPermissionCollection`。
- 已知名：`suppressAccessChecks`、`newProxyInPackage.<pkg>`。

## 2. Java 用户如何使用
安装 `SecurityManager` 的旧代码在策略文件中授予这些权限；当代主要是遗留库兼容与权限集合逻辑复用（BasicPermission 语义）。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/ReflectPermission.java`（含 @deprecated 注记）。
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/ReflectPermission/`。
- `java.lang.SecurityManager.checkPermission(new ReflectPermission("suppressAccessChecks"))` 历史调用点（AccessibleObject 源码邻接）。
- 旧版 Spring Security/Acegi 策略文件以 `ReflectPermission` 授予项出现（场景 1/6 同构）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ReflectPermissionTest.testBlackBox`）：
1. implies 格上已知目标：自反、异名不隐含、`*` 蕴含特定、特定不蕴含 `*`。
2. 层级名：`newProxyInPackage.*` 蕴含包级、兄弟包不隐含、自反。
3. 双参构造器 actions 忽略：equals/implies 双向、getActions 均归一为空。
4. equals/hashCode 契约（同名等、异名/异型不等、哈希一致）。
5. 名字校验：null → NPE、空串 → IAE。
6. `newPermissionCollection`：加入后蕴含、未加不蕴含、通配覆盖。
7. toString 含名字。
白盒保留原有构造器/名称断言 + 继承层次。产出：`ReflectPermissionTest.java`（30 断言通过；编译期有 deprecation 警告属预期，类在 JDK-27 标记 for removal）。
