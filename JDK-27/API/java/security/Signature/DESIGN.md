# Signature — 设计案

## 1. API 功能
数字签名引擎抽象类（extends SignatureSpi）。getInstance 工厂 + initSign/initVerify/update/sign/verify/getAlgorithm/getProvider/getParameters/toString/clone。

## 2. 使用方式
`Signature.getInstance("SHA256withRSA")` → `initSign(私钥)` → `update(数据)` → `sign()` 得签名；验签 `initVerify(公钥)` → `update(数据)` → `verify(签名)`。

## 3. 参考来源
- javadoc `java.security.Signature`（状态机：未 init 调用 sign/update/verify→SignatureException）。
- OpenJDK `test/jdk/java/security/Signature/`（SignatureTest、SignWithOutput 系列）。
- JCK runtime-8b `api/java_security/Signature/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 状态机：未 initSign 前 sign/update→SignatureException；未 initVerify 前 verify→SignatureException。
- 非法密钥：initSign(伪 PrivateKey)/initVerify(伪 PublicKey)→InvalidKeyException（不能用错误真实密钥，因签名重载按类型编译期匹配）。
- 签名往返：sign/verify 真；篡改数据/签名→false；initSign(私钥, SecureRandom) 往返；update(byte)/(byte[],off,len)/(ByteBuffer)。
- 边界：`update(null)`→NPE、`update(null,0,1)`→IAE、越界→IAE；`sign(短缓冲)`→SignatureException；`sign((byte[])null,off,len)`→IAE；`verify(null)`→NPE。
- 易错点：getParameters 对 SHA256withRSA 返回 null；clone() 抛 CloneNotSupportedException；verify(证书) 需证书与私钥匹配故不测。
