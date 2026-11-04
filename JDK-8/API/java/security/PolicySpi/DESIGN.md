# java.security.PolicySpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类（deprecated for removal），`Policy.getInstance` 装配的策略 SPI；public 无参构造。
- 唯一抽象方法 `engineImplies(ProtectionDomain,Permission)`；3 个默认：`engineRefresh()`（静默 no-op）、`engineGetPermissions(CodeSource)`、`engineGetPermissions(ProtectionDomain)`。

## 2. Java 用户如何使用
策略 provider 继承 PolicySpi 只需实现 engineImplies，经 `Policy.getInstance(type,params,provider)` 装配进 Policy 框架。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/PolicySpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/Policy/`
- 内部默认实现 `Policy$UnsupportedEmptyCollection`

## 4. 测试用例设计 + 自动化实现
- 结构：exact-1-abstract（engineImplies）、其余 3 个引擎方法 protected 非抽象。
- 透明记录子类：engineImplies 收到精确的 (pd,permission) 同一实例，null permission 也透传（由子类决定返回值）。
- 默认 `engineRefresh()`：静默返回，无任何副作用可断言（不抛即通过）。
- 默认 `engineGetPermissions` 两个重载返回**共享的同一个 `Policy$UnsupportedEmptyCollection` 实例**：`implies` 恒 false、`elements()` 空、`toString()` 非 null、**`add()` → `java.lang.SecurityException`（javadoc 提到不可添加，实测异常类型）**。

断言入口：`PolicySpiTest.testContractDeep()`（6→21 断言）。
