# Security — 设计案

## 1. API 功能
集中管理安全提供者与安全属性。getProviders/getProvider/getAlgorithms/getProviders(filter)/getProperty/setProperty。

## 2. 使用方式
`Security.getAlgorithms("MessageDigest")`、`Security.getProvider("SUN")`、`Security.getProviders("MessageDigest.SHA-256")`。

## 3. 参考来源
- javadoc `java.security.Security`。
- OpenJDK `test/jdk/java/security/Security/`。
- JCK runtime-8b `api/java_security/Security/`。

## 4. 测试用例设计 + 易错规范细节
- getProviders() 非空；getProviders(String filter) 用 "MessageDigest.SHA-256"（跨 JDK 稳定，SHA256 别名在 JDK-8 缺失）；getProviders(Map filter) 用 "MessageDigest.SHA-256"/"Cipher.AES"。
- getProvider：SUN 非 null；不存在→null（大小写敏感）。
- getAlgorithms：MessageDigest 含 MD5/SHA-256；Signature 含 SHA1WITHRSA/SHA256WITHRSA（返回大写）；Cipher 含 AES；未知服务/ null 服务→空集合。
- getProperty/setProperty 用唯一键往返并在 finally 恢复，避免污染全局。
- 易错点：getProviders("MessageDigest") 裸服务名是非法 filter（抛 InvalidParameterException）；"MessageDigest.SHA256" 在 JDK-8 抛 NPE 而 JDK-27 可用，故用 SHA-256。

## 5. 契约深化补充（本批）
- addProvider 槽位>0、同实例重复 add → -1、同名不同实例 → -1；insertProviderAt 位置返回与列表位置兑现；removeProvider 后 getProvider null、重复 remove 无害（try/finally 还原，不污染其它测试）。
- getProvider 大小写敏感、getProvider(null) → null（不抛，实测）；getProviders(未知 String 过滤) → null；getProviders(null String/null Map)/getProperty(null)/setProperty(null,·)/setProperty(·,null) → NPE；insertProviderAt(null,·) → NPE。
- getAlgorithms：类型大小写不敏感；算法名大写规范化（SHA256WITHDSA）；SHA1PRNG/JKS/PKCS12 存在断言。
- keystore.type 属性 == KeyStore.getDefaultType()（jks@8 vs pkcs12@27 由各自运行时兑现）。
