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
- JDK-27 默认类型断言为 pkcs12（与 JDK-8=jks 相对）。
- getCreationInstant：存在条目非 null 且不超前当前；缺失别名返回 null。
- getAttributes：存在与缺失别名均返回空集（不抛异常）。
- getInstance(File, char[])（JDK-9+）：先探测类型（JKS 文件→"JKS"）再装载，条目可见；临时文件用后删除。
- JKS load(LoadStoreParameter) 在 JDK-27 正常工作（JDK-8 为 UOE）。
- 其余维度（JKS 私钥闭环、setEntry 保护矩阵、大小写别名、空库往返、EOFException、JCEKS/PKCS12 密钥循环）见 §4 与用例。
- 补充：JKS 在 JDK-27 为已废弃但完全可用（deprecation only），本侧用例以 JKS 完成闭环验证。
