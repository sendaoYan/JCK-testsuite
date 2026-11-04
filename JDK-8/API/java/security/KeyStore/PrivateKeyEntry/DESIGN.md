# java.security.KeyStore.PrivateKeyEntry 黑盒行为测试设计

## 1. API 功能
私钥+证书链条目：`PrivateKeyEntry(PrivateKey, Certificate[] chain)`、`getPrivateKey()`、`getCertificateChain()`、`getCertificate()`（= chain[0]）、继承的 `getAttributes()`。

## 2. 使用方式
`new KeyStore.PrivateKeyEntry(priv, chain)` → `ks.setEntry(alias, entry, pp)` 或 `ks.setKeyEntry(alias, priv, pw, chain)`；JKS 要求私钥必须伴随证书链。

## 3. 参考来源
- javadoc `java.security.KeyStore.PrivateKeyEntry`。
- OpenJDK `test/jdk/java/security/KeyStore/PrivateKeyEntry/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 构造校验链（顺序敏感）：null key/chain → NPE("invalid null input")；零长链 → IAE("invalid zero-length input chain")；链含 null 元素 → NPE；证书 type 混合 → IAE("chain does not contain certificates of the same type")；私钥算法 ≠ chain[0] 公钥算法 → IAE("private key algorithm does not match…"）。
- 防御性拷贝双向：构造后改源数组、取回后改泄漏数组，条目均不受影响。
- identity 相等（无 equals 覆写）；默认属性集空。
- 端到端 JKS 闭环：setEntry→getEntry/getKey 跨实例 equals、双证书链往返、isKeyEntry true、错口令 UnrecoverableKeyException。
- 易错点：混合 type 用 getType()!="X.509" 的 FauxCert 触发（真 X.509 会先走 ArrayStoreException 分支）；FauxCert.getPublicKey() 不能返回 null（构造器会解引用）。

断言入口：`PrivateKeyEntryTest.java` 的 `testContractDeep()`（原有结构断言保留；RSA-512 密钥对 + 固定自签证书 fixture）。
