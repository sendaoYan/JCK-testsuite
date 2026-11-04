# java.security.KeyStore.Entry 黑盒行为测试设计

## 1. API 功能
密钥库条目标记接口。唯一声明方法为 default `getAttributes()`（返回 `Set<Attribute>`，默认空集）；三个实现类：PrivateKeyEntry、SecretKeyEntry、TrustedCertificateEntry。

## 2. 使用方式
作为 `KeyStore.getEntry/setEntry` 的统一条目类型；需要携带 PKCS12 属性时才关心 getAttributes，默认实现即可满足普通 JKS/JCEKS 流程。

## 3. 参考来源
- javadoc `java.security.KeyStore.Entry`。
- OpenJDK `test/jdk/java/security/KeyStore/Entry`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 结构：反射确认恰好 1 个声明方法且带 default 修饰；三种具体条目 instanceof Entry。
- 默认属性集不可变：`getAttributes().add(...)` → UnsupportedOperationException（底层 Collections.emptySet）。
- PKCS12 往返后属性集非空：friendlyName（OID 1.2.840.113549.1.9.20）值恒等于 alias（确定性可断言），localKeyId（1.2.840.113549.1.9.21）仅断言存在（值非确定），总数恰为 2。
- 易错点：JDK-8 SecretKeyEntry 无双参构造（attributes 版本），属性只能经 PKCS12 往返获得；断言属性值需按 DER 十六进制比较，直接字符串比较会失败。

断言入口：`EntryTest.java` 的 `testContractDeep()`（原有结构断言保留）。
