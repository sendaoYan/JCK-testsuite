# java.lang.Boolean 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parse 一个布尔」的冒烟。重点补：白盒结构（final、Object 超类、
> 实现 Comparable、TRUE static final、parseBoolean/valueOf 返回类型）、常量 TRUE/FALSE/TYPE、
> parseBoolean 大小写不敏感（含 null→false 不抛）、valueOf 单例缓存、hashCode 1231/1237、
> equals/compareTo/compare（false<true）、logicalAnd/Or/Xor 真值表、装箱拆箱、getBoolean（finally 恢复属性）。
> 断言确定，headless 安全，无异常路径（Boolean API 不抛）。

## 1. API 提供了哪些功能

- **常量**：`TRUE`、`FALSE`（单例）、`TYPE=boolean.class`。
- **工厂/解析**：`valueOf(boolean)`、`valueOf(String)`、`parseBoolean(String)`、`getBoolean(String)`。
- **对象**：`booleanValue`、`toString(boolean)`/`toString()`、`equals`、`hashCode`（1231/1237）、`hashCode(boolean)`。
- **比较/逻辑**：`compare`、`compareTo`、`logicalAnd`、`logicalOr`、`logicalXor`（JDK-8 已引入）。

## 2. Java 用户如何使用

- `Boolean.valueOf(true)` 恒返回 `TRUE` 单例；`Boolean.parseBoolean("true")` 大小写不敏感、其余（含 null）返回 false。
- 配置开关 `Boolean.parseBoolean(System.getProperty("debug"))`；逻辑组合 `Boolean.logicalAnd/Or/Xor`；排序 `Boolean.compare(false,true)<0`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Boolean.html`）：parseBoolean 大小写不敏感、valueOf 单例、
  hashCode 1231/1237、logicalAnd/Or/Xor。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Boolean.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Boolean`。

## 4. 测试用例设计（映射到 BooleanTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Object 超类、Comparable、TRUE static final、parseBoolean/valueOf 返回类型 | testWhiteBox |
| P0 | TRUE/FALSE/TYPE | testConstants |
| P0 | parseBoolean 大小写不敏感 + null→false | testParseBoolean |
| P0 | valueOf 单例缓存 | testValueOf |
| P1 | booleanValue/toString | testBooleanValueToString |
| P0 | hashCode 1231/1237 + equals | testHashCodeEquals |
| P0 | compareTo/compare（false<true）+ 反对称 | testCompare |
| P1 | logicalAnd/Or/Xor 真值表 | testLogicalCombinators |
| P1 | 装箱/拆箱 + valueOf(parseBoolean) 一致性 | testBoxing |
| P1 | getBoolean（finally 恢复系统属性） | testGetBoolean |

## 关键规范细节（易错，已锁入断言）

- **`parseBoolean(null)` 返回 `false`，不抛 NPE**（与 `Integer.parseInt(null)` 不同）。
- **`valueOf(boolean)` 恒返回 `TRUE`/`FALSE` 单例**（`==` 恒等）。
- **`hashCode`：true→1231、false→1237**（`hashCode(boolean)` 同）。
- **`compareTo`/`compare` 排序 false < true**。
- **`logicalAnd/Or/Xor` 是 Java 8 引入**，JDK-8 可用。

## 注意事项

- `System.setProperty` 用 try/finally 恢复，避免污染其他测试；headless 安全，不加 `@key`。
