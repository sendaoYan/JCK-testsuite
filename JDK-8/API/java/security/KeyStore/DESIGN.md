# KeyStore — 设计案

## 1. API 功能
密钥/证书仓库。getInstance 工厂（getInstance 异常是 KeyStoreException 而非 NoSuchAlgorithmException）+ load/store/getKey/getCertificate/setCertificateEntry/aliases/containsAlias/isKeyEntry/isCertificateEntry/getCreationDate/deleteEntry/size/getType/getProvider/getDefaultType。

## 2. 使用方式
`KeyStore.getInstance("PKCS12")` → `load(null,null)` 空库 → `setCertificateEntry(alias, cert)` → `store(out, pwd)` → `load(in, pwd)` 往返。

## 3. 参考来源
- javadoc `java.security.KeyStore`（getInstance→KeyStoreException）。
- OpenJDK `test/jdk/java/security/KeyStore/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→KeyStoreException；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 空库语义：size 0、aliases 空、getKey/getCertificate 返回 null、containsAlias/isKeyEntry/isCertificateEntry false。
- 证书条目生命周期：setCertificateEntry→getCertificate equals、isCertificateEntry true、isKeyEntry false、getCreationDate 非 null；deleteEntry 已存在删除、不存在 no-op。
- store/load 往返；store(null)→NPE。
- 易错点：getDefaultType 跨 JDK 不同（JDK-8=jks、JDK-27=pkcs12），不断言具体值；跨 JDK 统一用 PKCS12（JKS 在 JDK-27 已移除）；证书类型用 java.security.cert.Certificate 避免与 java.security.Certificate 歧义；嵌入固定自签证书保证确定性。


## 5. 契约深化补充（testContractDeep）
- 默认类型断言为 jks（JDK-8 专属，JDK-27=pkcs12）。
- 未初始化 store：containsAlias/getCertificate/isKeyEntry/deleteEntry → KeyStoreException("Uninitialized keystore")。
- JKS 私钥闭环：setKeyEntry(RSA-512)+链 → getKey/getEntry/getCertificate/getCertificateAlias/getCreationDate/size；跨实例 equals。
- setKeyEntry(privKey, pw, null chain) → IllegalArgumentException("Private key must be accompanied by certificate chain")；setKeyEntry(SecretKey) → KeyStoreException("Cannot store non-PrivateKeys")；setKeyEntry(byte[] 非 EPI) → KeyStoreException。
- getEntry 异常矩阵：key 条目 + CallbackHandlerProtection → UnsupportedOperationException；cert 条目 + PasswordProtection → UOE；missing alias → null。
- 字节级 store/load 往返；空库往返；3 字节垃圾 → EOFException；JKS 别名大小写不敏感 vs CaseExactJKS 敏感。
- JCEKS/PKCS12 SecretKey 闭环；store(LSP) → UOE；provider 固定工厂。
