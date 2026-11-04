# java.security.SignatureSpi

## 1. API 功能
数字签名算法的服务提供者接口。实现者需实现 `engineInitVerify`、`engineInitSign(PrivateKey)`、`engineUpdate(byte)`、`engineUpdate(byte[],int,int)`、`engineSign()`、`engineVerify(byte[])`、`engineSetParameter(String,Object)`、`engineGetParameter(String)`。类提供若干默认实现：`engineInitSign(PrivateKey,SecureRandom)` 会先赋值 `appRandom` 再调用单参版本；`engineSign(byte[],int,int)`/`engineVerify(byte[],int,int)` 默认基于数组版本；`engineSetParameter(AlgorithmParameterSpec)` 与 `engineGetParameters()` 默认抛 `UnsupportedOperationException`；`clone()` 在 `instanceof Cloneable` 时浅拷贝。

## 2. Java 用户如何使用
用户通过 `Signature.getInstance("SHA256withRSA")` 获取封装实例，调用 `initSign`/`update`/`sign`/`verify`，由 `Signature` 转发给 SPI。提供者子类实现具体签名/验签逻辑。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/SignatureSpi.java`（JDK 源）：
  - `protected void engineInitSign(PrivateKey privateKey, SecureRandom random) { this.appRandom = random; engineInitSign(privateKey); }`
  - `protected int engineSign(byte[] outbuf, int offset, int len) { byte[] sig = engineSign(); ... System.arraycopy(sig, 0, outbuf, offset, sig.length); return sig.length; }`
  - `protected void engineSetParameter(AlgorithmParameterSpec params) { throw new UnsupportedOperationException(); }`
- `test/jdk/java/security/Signature/SignatureGetInstance.java`：`public static class MySigImpl extends SignatureSpi { protected void engineInitVerify(...) {...} protected byte[] engineSign() {...} ... }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`、含 `clone` 方法。
- `testBlackBox()`：最小子类 `MySigSpi`（`engineSign()` 返回 `{4,5,6}`，`engineVerify` 相等比较）断言 `appRandom` 初值为 null、`engineInitSign(pk,random)` 后 `appRandom==random`、继承的 `engineSign(byte[],int,int)` 写入签名并返回长度、`engineVerify(byte[],int,int)` 正确匹配、默认 `engineSetParameter(AlgorithmParameterSpec)` 与 `engineGetParameters()` 抛 `UnsupportedOperationException`、`clone()` 返回不同实例。

## 5. 契约深化（testContractDeep，本轮新增）
- appRandom protected 字段默认 null；engineInitSign(k,r) 先存 appRandom 再委托；engineSign(buf,off,len) 两种精确 SignatureException 消息（partial/insufficient space）；engineVerify(sig,off,len) 精确切片透传；默认 engineSetParameter(spec)/engineGetParameters() → UnsupportedOperationException（消息 null）；默认 clone() → CNSE。
- 外观闭环：SHA256withDSA DSA-1024 签名/验签 true、异消息 false、篡改 DER 头 → SignatureException("Invalid encoding")。
