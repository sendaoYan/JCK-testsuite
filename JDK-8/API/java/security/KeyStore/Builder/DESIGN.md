# java.security.KeyStore.Builder 黑盒行为测试设计

## 1. API 功能
延迟构建 KeyStore 的抽象类：抽象方法 `getKeyStore()`（声明 KeyStoreException）与 `getProtectionParameter(String)`；静态工厂 `newInstance(KeyStore, ProtectionParameter)`、`newInstance(String type, Provider, ProtectionParameter)`、`newInstance(String type, Provider, File, ProtectionParameter)`、`newInstance(File, ProtectionParameter)`。

## 2. 使用方式
把 Builder 交给框架（如 PKCS12 keystores 管理），首次 `getKeyStore()` 才真正装载/构建；随后 `getProtectionParameter(alias)` 取各别名口令。

## 3. 参考来源
- javadoc `java.security.KeyStore.Builder`。
- OpenJDK `test/jdk/java/security/KeyStore/Builder/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 调用顺序契约：先 `getProtectionParameter` 后 `getKeyStore` → IllegalStateException("getKeyStore() must be called first")；getKeyStore 后保护参数与别名无关恒定。
- 包装变体：getKeyStore 两次同一实例（缓存）；未初始化 KeyStore → IllegalArgumentException；null protection → NPE。
- 4 参工厂 null file（非 DKS）→ NPE（构造期，非 IAE）；未知类型构造期接受、首次 getKeyStore 才 KeyStoreException（惰性失败）。
- File 变体：装载文件条目、缓存同一实例、store 是活对象（后续修改可见）。
- 类型变体：全新空 store、类型正确、可写；临时文件 createTempFile + deleteOnExit + finally delete 还原。
- 自定义子类：匿名继承记录 getKeyStore 调用次数，验证抽象契约。
- 易错点：createTempFile 前缀 ≥3 字符；Base64 证书常量须程序化提取。

断言入口：`BuilderTest.java` 的 `testContractDeep()`（原有结构断言保留）。
