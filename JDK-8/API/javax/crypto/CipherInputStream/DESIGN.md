# javax.crypto.CipherInputStream 黑盒行为测试设计

## 1. API 功能
- 构造：`CipherInputStream(InputStream, Cipher)`（public，Cipher 须已 init）、`CipherInputStream(InputStream)`（protected，内部用 NullCipher）。
- 读：`read()`（单字节 0–255/-1）、`read(byte[])`、`read(byte[], int, int)`（len==0 返回 0）。
- 流控制：`skip(long)`（只跳过**已处理未读出**的缓冲字节）、`available()`（只计已过 cipher 的字节数）、`close()`（幂等；关闭时不抛解密异常）、`markSupported()`==false。

## 2. Java 用户如何使用
```java
Cipher d = Cipher.getInstance("AES/CBC/PKCS5Padding");
d.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
try (CipherInputStream cis = new CipherInputStream(in, d)) {
    int b; while ((b = cis.read()) != -1) out.write(b);   // 或 read(byte[]) 循环
}
```
典型链路：COS 加密成 byte[] → ByteArrayInputStream → CIS 解密逐字节/read(buf) 循环 → 与明文逐字节相等。GCM 模式下 AEAD 解密在缓冲充满前不吐数据，首次 read() 内部读完整个流并验证 tag。

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/test/jdk/javax/crypto/Cipher/CipherInputStreamExceptions.java` L63–90 `gcm_AEADBadTag()`：篡改 tag 后 `read()` 抛 `IOException`，`getCause() instanceof AEADBadTagException`；L143+ 证明 close() 不抛（本测试同款断言，实测一致）。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/Cipher/CipherStreamClose.java` L112–126 `streamDecrypt`：CIS + ObjectInputStream 反序列化（try-with-resources 流式用法）。
- `~/git/jdk8u-dev/test/jdk/com/sun/crypto/provider/CICO/PBEFunc/CipherNCFuncTest.java`：NullCipher + CICO 透传功能测试。
- 行为权威：`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/CipherInputStream.java`（getMoreData L144：AEAD 解密在 read 时整流验证；close L352 捕获全部解密异常）。
- Spring Security crypto / 常见 GitHub 文件加密工具（web 调研）用 CIS 做大文件流式 AES-GCM 解密，并依赖"tag 校验失败在流末尾抛出"的语义（[Spring Security Crypto](https://docs.spring.io/spring-security/reference/features-integrations/cryptography.html)）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`CipherInputStreamTest.testBlackBox`，保留原有 NullCipher 透传 4 条断言）：
- CBC/PKCS5 37B：逐字节 read 与 read(byte[7]) 循环两种方式均精确还原；COS 加密的密文作输入（组合链路）。
- available() 语义：首字节读出后 ==31（PKCS5 解密暂扣最后一块，32-1）；EOF 后 ==0；GCM 首次 read 后 ==36（整段已认证缓存）。
- skip(3)：跳过的是明文流内 3 字节，继续读到精确 EOF（总长 37）且内容索引对位。
- GCM 37B：逐字节与 read(byte[16]) 循环往返均成立。
- 篡改 GCM 密文首字节：`read()` 抛 IOException 且 cause 为 AEADBadTagException；此后 close() 不抛；未读过直接 close() 也不抛（异常抑制）。
- NullCipher 后端：markSupported()==false；读前 available()==0；read 后 4；skip(2)+read 对位；read(b,off,0)==0 不消耗；EOF 三态（read()/read(byte[])/available 全 -1/-1/0）；二次 close 无害。
- protected 构造器经局部子类调用：NullCipher 透传（9,8,-1）。
- 空明文加密流 → 首次 read()==-1。
白盒场景（`testWhiteBox`）：public 具体、父类 java.io.FilterInputStream（二进制名）、public/protected 构造器形态、read x3 + skip/available/close/markSupported 存在。
实测校准：available==31（而非 37-1=36）系 PKCS5 解密暂扣末块所致，已按 JDK-27 实测断言。
