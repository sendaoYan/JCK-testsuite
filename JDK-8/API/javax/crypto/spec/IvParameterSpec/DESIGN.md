# javax.crypto.spec.IvParameterSpec 设计案

## 1. API 功能
初始化向量（IV）规范（实现 `AlgorithmParameterSpec`，since 1.4），用于 CBC/CFB/OFB 等反馈模式：
- 构造：`IvParameterSpec(byte[] iv)`；`IvParameterSpec(byte[] iv, int offset, int len)`（取子区间）。两者均防御拷贝。
- 查询：`getIV()`（每次返回新数组）。
- 类级校验：1 参构造 null → `NullPointerException`（`this(iv,0,iv.length)` 触发）；3 参构造 null → `IllegalArgumentException`，负 offset/len → `ArrayIndexOutOfBoundsException`，`iv.length-offset < len` → `IllegalArgumentException`。

## 2. Java 用户如何使用
```java
IvParameterSpec ivSpec = new IvParameterSpec(iv16);
Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
c.init(Cipher.ENCRYPT_MODE, key, ivSpec);
byte[] ct = c.doFinal(pt);
c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv16));
byte[] pt2 = c.doFinal(ct);
```
典型链路：构造 spec → `Cipher.init` → 加密 → `Cipher.getIV()` 应与 spec 内容一致 → 同 IV 重加密结果确定（CBC 在固定 key+IV 下确定）→ 解密往返。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/IvParameterSpec/NullIV.java`（L33-45）：1 参 null → NPE、3 参 null → IAE——本设计异常断言对齐。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/IvParameterSpec/InvalidArgs.java`（L33-45）：负 offset/len → ArrayIndexOutOfBoundsException。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/IvParameterSpec.java`（L75-91）：异常条件与防御拷贝实现。
- Web 调研：`AES/CBC/PKCS5Padding` + `IvParameterSpec` 是 Java 加解密工具类最常见组合之一（各类 AES 工具库/Stack Overflow 高票答案的标准写法）。

## 4. 测试用例设计 + 自动化实现
`IvParameterSpecTest.java`：
- **白盒**：public/非 final、父类 Object、实现 AlgorithmParameterSpec、两个公共构造器、getIV 存在。
- **黑盒**：
  1. 基本构造 getIV、offset 构造器长度（保留原有 2 断言）；
  2. 防御拷贝三连（外部改、两次 getter 非同一引用、改返回值）；
  3. offset 精确子区间；零长 IV 类级接受；
  4. 异常矩阵 5 项：NPE（1 参 null）、IAE（3 参 null、offset+len 越界）、AIOOBE（负 offset、负 len）；
  5. 真实链路：AES/CBC/PKCS5Padding 固定 key/IV/PT → KAT 密文 `f9a20cc5...269c896ee`、`Cipher.getIV()`==spec 内容、同 IV 重加密相等、往返、换 IV 密文不同；
  6. NoPadding 块对齐往返（offset 构造器提供的 IV）。
