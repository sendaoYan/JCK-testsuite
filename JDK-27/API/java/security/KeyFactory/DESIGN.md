# KeyFactory — 设计案

## 1. API 功能
密钥工厂（密钥规范↔密钥对象转换）。getInstance 工厂 + generatePublic/generatePrivate/getKeySpec/translateKey/getAlgorithm/getProvider。

## 2. 使用方式
`KeyFactory.getInstance("RSA")` → `getKeySpec(公钥, X509EncodedKeySpec.class)` → `generatePublic(spec)`；私钥对应 PKCS8EncodedKeySpec。

## 3. 参考来源
- javadoc `java.security.KeyFactory`（无效 spec→InvalidKeySpecException）。
- OpenJDK `test/jdk/java/security/KeyFactory/`。
- JCK runtime-8b `api/java_security/KeyFactory/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 往返：getKeySpec(generatePublic(spec)) 与原始公钥 equals；私钥同理；从 getEncoded 重新构造。
- 异常：getKeySpec 错误 spec 类/空 spec 类/空 key→InvalidKeySpecException 或 NPE；generatePublic/Private(null 或错误 spec)→InvalidKeySpecException；translateKey(null)→InvalidKeyException。
- 易错点：getKeySpec(public, null class) 需显式泛型 null 避免编译推断失败。
