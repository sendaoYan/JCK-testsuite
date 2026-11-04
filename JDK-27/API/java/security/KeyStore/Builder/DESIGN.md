# java.security.KeyStore.Builder 黑盒行为测试设计

## 1. API 功能
- 抽象：`getKeyStore()`、`getProtectionParameter(String)`
- 工厂：`newInstance(KeyStore, ProtectionParameter)`、`newInstance(String, Provider, ProtectionParameter)`、`newInstance(String, Provider, File, ProtectionParameter)`、`newInstance(File, ProtectionParameter)`

## 2. Java 用户如何使用
延迟构建：先把 Builder 交给框架，首次 getKeyStore 才装载；File 变体按文件探测类型。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（FileBuilder 缓存实例；type-based 匿名类每次新建 store；getProtectionParameter 需先 getKeyStore）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/Builder/`（Builder 用法与口令获取）

## 4. 测试用例设计 + 自动化实现
- 正常：固定 store 变体每次返回同一实例且 protection 恒定
- 组合：type 变体每次 getKeyStore 返回新 store（JDK-27 源码行为）；File 变体装载文件条目；File-only 变体探测类型为 PKCS12
- 异常：未先 getKeyStore 调 getProtectionParameter → IllegalStateException；null alias → NPE；未初始化 KeyStore 传入 → IllegalArgumentException

断言入口：`BuilderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（testContractDeep）
- 包装工厂 null protection → NPE；4 参工厂 null file（非 DKS）→ 构造期 NPE。
- CallbackHandlerProtection 变体包装往返一致。
- JKS File 变体：装载条目、缓存同一实例、store 活对象（修改可见）。
- 未知类型构造期接受、首次 getKeyStore 才 KeyStoreException（惰性失败）。
- 自定义匿名子类记录 getKeyStore 调用次数，验证抽象契约。
