# java.security.KeyStore.TrustedCertificateEntry 黑盒行为测试设计

## 1. API 功能
受信证书条目：构造器 `TrustedCertificateEntry(Certificate)`、`getTrustedCertificate()`、继承的 `getAttributes()`。

## 2. 使用方式
`new KeyStore.TrustedCertificateEntry(cert)` → `ks.setEntry(alias, entry, null)`（null 保护是受信条目唯一合法值）或 `ks.setCertificateEntry(alias, cert)`。

## 3. 参考来源
- javadoc `java.security.KeyStore.TrustedCertificateEntry`。
- OpenJDK `test/jdk/java/security/KeyStore/TrustedCertificateEntry/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 构造 null → NullPointerException("invalid null input")；getTrustedCertificate 返回的证书与重解析实例值相等；默认属性集空；toString 非空；identity 相等。
- JKS 闭环（null 保护双向）：setEntry→getEntry 类型往返、isCertificateEntry true/isKeyEntry false、getCertificate equals、getCertificateChain null、getKey null、getCertificateAlias 命中、size/containsAlias。
- 口令保护双向拒绝：setEntry 带 PasswordProtection → KeyStoreException("trusted certificate entries are not password-protected")；getEntry 带 PasswordProtection → UnsupportedOperationException（同一文案）。
- 生命周期：同 alias 覆盖 size 不变；deleteEntry 后 getEntry null。
- 易错点：null 与口令保护的适用方向（key 条目相反）；证书 fixture 必须程序化提取 Base64，手抄必错。

断言入口：`TrustedCertificateEntryTest.java` 的 `testContractDeep()`（原有结构断言保留）。
