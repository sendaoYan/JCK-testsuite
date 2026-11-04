# java.security.KeyStore.LoadStoreParameter 黑盒行为测试设计

## 1. API 功能
load/store 的参数化标记接口。唯一方法 `getProtectionParameter()` 返回 ProtectionParameter；实现类 DomainLoadStoreParameter（DKS 域密钥库）。

## 2. 使用方式
`ks.load(lsp)` / `ks.store(lsp)` 替代显式口令数组，由 SPI 决定是否支持。

## 3. 参考来源
- javadoc `java.security.KeyStore.LoadStoreParameter`、`DomainLoadStoreParameter`。
- OpenJDK `test/jdk/java/security/KeyStore/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 结构：恰好 1 个声明方法；自定义实现固定身份返回。
- JDK-8 关键差异：JKS `load(LSP)` → UnsupportedOperationException（JDK-9+ 才支持）；PKCS12/JKS `store(LSP)` → UOE。
- DomainLoadStoreParameter：getConfiguration 返回构造 URI；getProtectionParams 为快照（改动源 map 不影响返回集合）；getProtectionParameter() 为 null（域级保护由条目级提供）。
- 易错点：不要在 JDK-8 断言 load(LSP) 成功；DKS 仅在 SUN provider 注册，构造 DomainLoadStoreParameter 不需要 DKS keystore 存在。

断言入口：`LoadStoreParameterTest.java` 的 `testContractDeep()`（原有结构断言保留）。
- 补充：JCEKS 同样 load/store(LSP) UOE；DomainLoadStoreParameter 保护 map 为构造期快照 + 不可修改视图（put → UOE）；接口无父接口。
