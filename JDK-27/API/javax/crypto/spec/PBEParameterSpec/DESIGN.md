# javax.crypto.spec.PBEParameterSpec 设计案

## 1. API 功能
口令加密（PBE, PKCS#5）参数规范（实现 `AlgorithmParameterSpec`，since 1.4）：
- 构造：`PBEParameterSpec(byte[] salt, int iterationCount)`；`PBEParameterSpec(byte[] salt, int iterationCount, AlgorithmParameterSpec paramSpec)`（since 1.8，嵌套底层密码参数如 IvParameterSpec）。salt 防御拷贝，null salt → NPE（`salt.clone()`）。
- 查询：`getSalt()`（每次新数组）；`getIterationCount()`；`getParameterSpec()`（无则 null）。
- 类级不做任何值域校验（iterationCount=0、空 salt 均可构造），合法性由 KDF/Cipher 执行。

## 2. Java 用户如何使用
```java
PBEKeySpec ks = new PBEKeySpec(password, salt, 2048);          // 与本类配套的 KeySpec
SecretKey key = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128")
                          .generateSecret(ks);
Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
c.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, 2048));
byte[] ct = c.doFinal(pt);  byte[] iv = c.getIV();
c.init(Cipher.DECRYPT_MODE, key,
        new PBEParameterSpec(salt, 2048, new IvParameterSpec(iv)));  // 3 参构造器承载 IV
byte[] pt2 = c.doFinal(ct);
```
典型链路：PBEKeySpec → SecretKeyFactory → PBEKey → Cipher.init(key, PBEParameterSpec) 加密 → 解密时同一 spec 类嵌套 IV 完成往返。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/Cipher/PBE/PBES2Test.java`（L76-93）：`new PBEKeySpec("mypassword".toCharArray())` + `pbeCipher.init(ENCRYPT_MODE, pbeKey, new PBEParameterSpec(salt, iterationCount, new IvParameterSpec(ivBytes)))`——本设计真实链路直接对齐（L91-93）。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/PBEParameterSpec.java`（L57-110）：防御拷贝、无值域校验、paramSpec 同一引用返回。
- Web 调研（DuckDuckGo 搜索）：`PBEKeySpec` + `SecretKeyFactory("PBKDF2WithHmacSHA256")` 是 Spring/Baeldung 类教程中口令哈希派生的标准写法；PBES2 加解密则普遍以 PBEParameterSpec 携带 salt+iterationCount。

## 4. 测试用例设计 + 自动化实现
`PBEParameterSpecTest.java`：
- **白盒**：public/非 final、父类 Object、实现 AlgorithmParameterSpec、两个公共构造器、getSalt/getIterationCount/getParameterSpec 存在。
- **黑盒**：
  1. 基本构造 getSalt/getIterationCount/getParameterSpec==null（保留原有 3 断言）；
  2. 防御拷贝三连（外部改 salt、两次 getSalt 非同一引用内容相等、改返回值）；
  3. 类级不做校验：iterationCount=0、空 salt 均可构造（文档化"延迟校验"）；
  4. 3 参构造器：嵌套 spec 同一引用、salt/iteration 保持、显式 null 允许；
  5. null salt（两个构造器）→ NPE；
  6. 真实链路：PBEWithHmacSHA256AndAES_128 加密（2 参 spec）→ getIV() 16B → 解密（3 参 spec 嵌 IvParameterSpec）往返；iterationCount+1 解密 → BadPaddingException（派生密钥不同）。
