# javax.crypto.CipherOutputStream 黑盒行为测试设计

## 1. API 功能
- 构造：`CipherOutputStream(OutputStream, Cipher)`（public，Cipher 须已 init）、`CipherOutputStream(OutputStream)`（protected，内部用 NullCipher）。
- 写：`write(int)`（单字节）、`write(byte[])`、`write(byte[], int, int)`；每次 write 即调用 `cipher.update` 并把已产出字节立即写到底层流。
- `flush()`：只 flush 底层流，**不**吐出 cipher 内缓冲的不完整块。
- `close()`：调用 `cipher.doFinal` 吐出尾块/填充/tag 后 flush 并关闭底层流；幂等；**捕获并吞掉** IllegalBlockSize/BadPadding/ShortBuffer 异常（与 CIS 的 read 语义关键差异）。

## 2. Java 用户如何使用
```java
Cipher e = Cipher.getInstance("AES/CBC/PKCS5Padding");
e.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
try (CipherOutputStream cos = new CipherOutputStream(out, e)) {
    cos.write(plaintext);      // 分块写亦可
}                              // close() 补 padding，数据此刻才完整
```
典型链路：COS 加密 → byte[] → CIS/Cipher.doFinal 解密还原；比较"COS 分块写"与"Cipher.doFinal 一次性"输出逐字节一致。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/CipherStreamClose.java` L90–110 `streamEncrypt`：COS 套 ObjectOutputStream 写对象、try-with-resources close 落盘（与本测试"COS == doFinal"同型）。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/CipherInputStreamExceptions.java`：对照 CIS 的异常语义，COS 侧 close 吞异常的来源。
- 行为权威：`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/CipherOutputStream.java`（close L259–285：`catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {}` 空捕获；flush L237 只调 output.flush）。
- Spring Security `AesBytesEncryptor` 与常见 GitHub 流式文件加密工具（web 调研）以 COS 包 BufferedOutputStream/FileOutputStream 做大文件 AES-GCM 加密（[Spring Security Crypto](https://docs.spring.io/spring-security/reference/features-integrations/cryptography.html)）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`CipherOutputStreamTest.testBlackBox`，保留原有 NullCipher 透传断言）：
- CBC/PKCS5 37B：write 后 flush 底层只见 32B（2 整块，5B 留在 cipher 缓冲——flush 不吐 cipher 缓冲）；close 后 48B（补一个 padding 块）；二次 close 幂等；所得密文再经 CIS 解密精确还原（跨类组合链路）。
- 写入形态等价性：分块 5+10+22、单字节 write(int)x10+27、一次性 write(37) 三者密文逐字节相同；且与直接 `Cipher.doFinal(37B)` 输出相同。
- GCM：COS 分块写（20+17）close 后输出（含 16B tag）与 `Cipher.doFinal` 完全一致。
- **GCM 篡改 + 解密方向 COS（关键差异，实测为准）**：写入篡改密文后 close() **不抛任何异常**（吞掉 AEADBadTagException），且底层输出为 0 字节（无明文泄漏）；close 幂等。DESIGN 与用例均记录：认证失败的暴露点在 CIS.read / Cipher.doFinal，不在 COS.close。
- protected 构造器经局部子类：NullCipher 透传（[7,7]）。
- NullCipher COS 三种 write 形态叠加 == "ABCD"。
- 空明文 close：CBC/PKCS5 仍吐整整一个 padding 块（16B）。
白盒场景（`testWhiteBox`）：public 具体、父类 java.io.FilterOutputStream（二进制名）、public/protected 构造器形态、write x3 + flush/close 存在。
