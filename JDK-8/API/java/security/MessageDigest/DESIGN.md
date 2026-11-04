# MessageDigest — 设计案

## 1. API 功能
消息摘要引擎抽象类（extends MessageDigestSpi）。getInstance 工厂 + update/digest/reset/clone/isEqual/getAlgorithm/getProvider/getDigestLength/toString。

## 2. 使用方式
`MessageDigest.getInstance("SHA-256")` → `update(byte[])`（或 `update(byte[],off,len)`/`update(ByteBuffer)`）→ `digest()` 得固定长度字节数组；`reset()` 复用；`isEqual` 恒定时比较。

## 3. 参考来源
- javadoc `java.security.MessageDigest`（异常契约：getInstance→NoSuchAlgorithmException/NoSuchProviderException，digest 短缓冲→DigestException）。
- OpenJDK `test/jdk/java/security/MessageDigest/`（KnownAnswer、TestDigest 系列）。
- JCK runtime-8b `api/java_security/MessageDigest/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null 算法→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 已知向量：MD5/SHA-1/SHA-256 的空串与 "abc" 十六进制摘要。
- 边界：`update(byte[],off,len)` 越界/负 offset/负 len→IAE 或 AIOOBE（JDK-8 实测负值落入 engineUpdate→ArrayIndexOutOfBoundsException）；`update(null)` 单参→NPE、`update(null,0,1)`→IAE；`digest(buf,off,len)` 短缓冲→DigestException。
- 状态不变量：digest 后自动 reset（连续 digest() 相同）；digest 后 update 可复用；reset 清空；clone 独立性。
- 易错点：`digest(byte[] input)` 是 update+digest（把入参当数据），不是写输出缓冲；toString 为 "算法 Message Digest from 提供者" 而非 "MessageDigest 算法"；JDK-8 无 "SHA256" 别名（用 "SHA1" 做别名等价）；跨 JDK 负 offset 异常类型一致为 AIOOBE。
