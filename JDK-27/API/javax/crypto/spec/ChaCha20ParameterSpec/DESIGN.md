# javax.crypto.spec.ChaCha20ParameterSpec 设计案

## 1. API 功能
ChaCha20（RFC 7539）参数规范（public final，实现 `AlgorithmParameterSpec`，since 11）：
- 构造：`ChaCha20ParameterSpec(byte[] nonce, int counter)`——nonce 必须 12 字节（null → NPE，长度≠12 → IllegalArgumentException），防御拷贝；counter 为 32 位初始计数。
- 查询：`getNonce()`（每次新数组）；`getCounter()`。

## 2. Java 用户如何使用
```java
ChaCha20ParameterSpec spec = new ChaCha20ParameterSpec(nonce12, 1);
Cipher c = Cipher.getInstance("ChaCha20");           // 流密码，无 padding
c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key32, "ChaCha20"), spec);
byte[] ct = c.doFinal(pt);                            // ct 长度 == pt 长度
c.init(Cipher.DECRYPT_MODE, key, new ChaCha20ParameterSpec(nonce12, 1));
byte[] pt2 = c.doFinal(ct);                           // 同 nonce+counter 才可逆
```
典型链路：固定 nonce/counter → init → KAT/往返 → 换 counter 或 nonce 均得到错误明文（密钥流不同）。流密码在固定 key/nonce/counter 下完全确定。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/Cipher/ChaCha20/ChaCha20KAT.java`（L76-96：RFC 7539 样例向量——key `00..1f`、nonce `000000000000004a00000000`、counter=1、明文 sunscreen 文本、期望密文 `6e2e359a...5e42874d`；L97-105：全零向量 counter=0 → `76b8e0ad...b2ee6586`；L249-253 `ChaCha20ParameterSpec` init 链路）——本设计两个 KAT 直接取自该文件。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/ChaCha20ParameterSpec.java`（L65-74）：`Objects.requireNonNull(nonce)` + 长度≠12 抛 IAE + `nonce.clone()`。
- JDK 自带测试 `test/jdk/com/sun/crypto/provider/Cipher/ChaCha20/ChaCha20NoReuse.java`：nonce/counter 重用与更换的行为。
- Web 调研：开源项目用 ChaCha20ParameterSpec 的场景多为固定 nonce 的确定性流加密（如本地文件混淆）或测试向量复现；TLS 侧更多用 ChaCha20-Poly1305（IvParameterSpec 12B nonce，见 ChaCha20Poly1305ParamTest.java L426）。

## 4. 测试用例设计 + 自动化实现
`ChaCha20ParameterSpecTest.java`：
- **白盒**：public final、父类 Object、实现 AlgorithmParameterSpec、公共 ctor(byte[],int)、getNonce/getCounter 存在且 getCounter 返回 int。
- **黑盒**：
  1. 基本 getNonce/getCounter（保留原有 2 断言）；
  2. 防御拷贝三连（构造后外部改 nonce 不影响、两次 getNonce 非同一引用内容相等、改返回值脱离内部）；
  3. nonce 长度矩阵 {0,1,11,13,16} → IllegalArgumentException；null → NPE；
  4. 真实链路 KAT：RFC 7539 样例向量（12B nonce + counter=1）精确密文；同参数重加密相等；解密往返；counter=2 / 全零 nonce 解密得错误明文；
  5. 第二 KAT：全零 key/nonce/明文 + counter=0 → `76b8e0ad...`；往返；流密码长度保持（64B）。
