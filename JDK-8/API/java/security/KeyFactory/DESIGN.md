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

## 5. 契约深化补充（本批）
- DSA 工厂全环：getKeySpec→generatePublic/Private equals、X509/PKCS8 编码重建 equals、p=1024/q=160、format 精确（X.509 / PKCS#8）。
- RSA 值级往返：RSAPublicKeySpec modulus=1024 位/exponent=65537；(n,d) 最小规格重建非 CRT 私钥——签名仍可验证但与原 CRT 钥不等。
- 跨算法：RSA 工厂吃 DSA 钥 → getKeySpec InvalidKeySpecException / translateKey InvalidKeyException；跨工厂实例 generatePublic 值相等。
