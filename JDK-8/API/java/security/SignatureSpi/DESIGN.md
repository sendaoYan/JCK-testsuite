# java.security.SignatureSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`Signature` 外观的 SPI 后端；public 无参构造。
- 抽象方法：`engineInitVerify`、`engineInitSign`、`engineUpdate(byte)`、`engineUpdate(byte[],int,int)`、`engineSign()`、`engineVerify(byte[])`。
- 默认实现：`engineInitSign(priv,random)`（先存 `appRandom` 再委托）、`engineSign(buf,off,len)`、`engineVerify(sig,off,len)`（切片后委托）、`engineUpdate(ByteBuffer)`（批量）、`engineSetParameter(spec)`/`engineGetParameters()`（JDK-8 前者抽象、后者可选）、`clone()`。
- protected 字段 `appRandom`（SecureRandom），默认 null。

## 2. Java 用户如何使用
provider 继承 SignatureSpi 实现引擎方法，注册后由 `Signature.getInstance("SHA256withDSA")` 装配；签名/验签经外观调用。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/SignatureSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/Signature/`
- DSS 实现 `sun.security.provider.DSA`（appRandom 使用参照）

## 4. 测试用例设计 + 自动化实现
- 结构：abstract、`appRandom` 为 protected 字段且默认 null、`engineInitSign(PrivateKey,SecureRandom)` 非 abstract、抽象方法精确清单。
- 透明记录子类（FakeKey 同时实现 PrivateKey+PublicKey）：断言 `engineInitSign(k,r)` 先写 appRandom 再委托；`engineSign(buf,off,len)` 默认异常矩阵：len<sigLen → `SignatureException("partial signatures not returned")`、空间不足 → `SignatureException("insufficient space in the output buffer to store the signature")`；`engineVerify(sig,off,len)` 切片后委托（子类收到精确切片内容）；`engineSetParameter(spec)` 默认 → `UnsupportedOperationException`（消息为 null）。
- 默认 `engineUpdate(ByteBuffer)` 批量转调数组变体；默认 `clone()` → CNSE。
- 外观闭环：`SHA256withDSA` DSA-1024 签名/验签 true、不同消息 false、**篡改 DER 头字节（ASN.1 tag）→ `SignatureException("Invalid encoding")` 而非 verify=false**。

断言入口：`SignatureSpiTest.testContractDeep()`（12→42 断言）。
