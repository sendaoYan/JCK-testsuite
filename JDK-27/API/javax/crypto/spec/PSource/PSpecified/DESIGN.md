# javax.crypto.spec.PSource.PSpecified 设计案

## 1. API 功能
`PSource` 的显式编码输入 P 取值类（public static final，since 1.5）：
- 构造：`PSpecified(byte[] p)`（null → NPE；内容防御拷贝）。
- 常量：`PSpecified.DEFAULT`——值等于空字节串 `byte[0]`。
- 查询：继承 `getAlgorithm()` 恒返回 `"PSpecified"`；`getValue()` 返回编码输入 P 的拷贝（空值时实现共享同一空数组，只保证内容相等）。

## 2. Java 用户如何使用
```java
PSource.PSpecified empty = PSource.PSpecified.DEFAULT;     // PKCS#1 默认空 label
PSource.PSpecified labeled = new PSource.PSpecified(label); // 显式 label
OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
        MGF1ParameterSpec.SHA256, labeled);
Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
c.init(Cipher.ENCRYPT_MODE, pub, spec);  // 加密
c.init(Cipher.DECRYPT_MODE, priv, spec); // 解密：label 必须与加密时一致
```
典型链路：带 label 的 PSpecified → OAEPParameterSpec → RSA-OAEP 加密 → 相同 label 解密成功 / 不同 label（DEFAULT）解密抛 BadPaddingException。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/Cipher/RSA/TestOAEPParameterSpec.java`（L45-46）：`new OAEPParameterSpec(mdName, "MGF1", mgfSpec, new PSource.PSpecified(p))`，p 为显式 label 字节。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/PSource.java`（L104-115）：ctor `p.clone()`、getValue 空数组共享实现、DEFAULT 常量。
- JDK 自带测试 `test/jdk/com/sun/crypto/provider/Cipher/RSA/TestOAEPPadding.java` / `TestOAEPWithParams.java`：`PSource.PSpecified.DEFAULT` 与显式 label 在 Cipher.init(…, OAEPParameterSpec) 链路中的用法。
- Web 调研：开源 RSA-OAEP 工具类普遍用 `PSource.PSpecified.DEFAULT`（默认）或固定 label 的 `new PSource.PSpecified(label)`。

## 4. 测试用例设计 + 自动化实现
`PSpecifiedTest.java`（目录 `javax.crypto.spec.PSource.PSpecified/`，二进制名 `javax.crypto.spec.PSource$PSpecified`）：
- **白盒**：声明类 PSource、public static final、extends PSource、公共 ctor(byte[])、DEFAULT 字段 public static final 且类型 PSpecified、getValue 存在。
- **黑盒**：
  1. DEFAULT 非空 + 空 value + 算法名（保留原有 4 断言）；
  2. 防御拷贝三连（外部改、两次 getter 非同一引用内容相等、改返回值）；
  3. 显式空 value 允许；null → NPE；无 equals 覆写（identity）；
  4. instanceof PSource；
  5. 真实链路：8 字节 label 的 PSpecified → RSA-2048 OAEPWithSHA-256 加密；等价 label 的另一实例解密成功；空 label 解密抛 BadPaddingException。
