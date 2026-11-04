# java.security.KeyStore.LoadStoreParameter 黑盒行为测试设计

## 1. API 功能
- 接口：`getProtectionParameter()`；实现：DomainLoadStoreParameter

## 2. Java 用户如何使用
把"如何保护这次 load/store"参数化；`ks.load(lsp)`/`ks.store(lsp)`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（SimpleLoadStoreParameter、engineLoad/engineStore(LSP)）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/sun/security/provider/KeyStore/DKSTest.java`（DKS 用 LSP 装载）

## 4. 测试用例设计 + 自动化实现
- 正常：DomainLoadStoreParameter 实现本接口且 keystore 条目 protection 为 null（条目保护在 getProtectionParams）
- 组合：自定义 LSP(PasswordProtection(null)) 驱动 `load` 初始化空库；PKCS12 不支持 store(LSP) → UnsupportedOperationException（默认 engineStore 抛出）

断言入口：`LoadStoreParameterTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
