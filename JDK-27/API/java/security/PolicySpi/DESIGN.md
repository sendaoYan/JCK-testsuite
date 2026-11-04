# java.security.PolicySpi 黑盒行为测试设计

## 1. API 功能
- protected 引擎方法：`engineImplies(ProtectionDomain, Permission)`（abstract）、`engineGetPermissions(CodeSource|ProtectionDomain)`、`engineRefresh()`

## 2. Java 用户如何使用
provider 通过 `Policy.getInstance` 装配 PolicySpi；子类覆写引擎方法实现策略逻辑。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PolicySpi.java`（deprecated for removal）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/Policy/`（策略装配相关）

## 4. 测试用例设计 + 自动化实现
- 白盒：abstract、engineImplies abstract、其余 protected 非抽象
- 黑盒：子类 public 覆写暴露引擎行为——engineImplies 按权限名前缀返回、engineGetPermissions(CodeSource) 返回定制集合、engineRefresh 状态可观测；默认 engineGetPermissions(ProtectionDomain) 返回非 null 空集合

断言入口：`PolicySpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化（testContractDeep，本轮新增）
- exact-1-abstract（engineImplies）；3 个默认方法 protected 非抽象结构断言。
- engineImplies 精确透传（pd/permission 同一实例，null permission 由子类决定返回值）；engineRefresh 静默 no-op。
- 两个 engineGetPermissions 重载返回共享同一 Policy$UnsupportedEmptyCollection：implies 恒 false、elements() 空、toString 非 null、add() → java.lang.SecurityException。
