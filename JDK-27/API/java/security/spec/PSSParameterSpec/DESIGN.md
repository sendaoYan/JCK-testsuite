# java.security.spec.PSSParameterSpec 测试设计

## 1. API 功能
`PSSParameterSpec` 实现 `AlgorithmParameterSpec`，描述 RSASSA-PSS 签名的参数：摘要算法、掩码生成函数 MGF、MGF 参数、盐长度与 trailer 字段。提供常量 `TRAILER_FIELD_BC`（=1）、`DEFAULT`（SHA-1/MGF1/盐长 20/trailer 1），以及完整 5 参构造器与简化的 `int saltLen` 构造器（其余取默认 SHA-1/MGF1/trailer 1）。

## 2. Java 用户如何使用
```java
PSSParameterSpec spec = new PSSParameterSpec(
    "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32,
    PSSParameterSpec.TRAILER_FIELD_BC);
int saltLen = spec.getSaltLength();
String digest = spec.getDigestAlgorithm();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/xml/crypto/dsig/PSSSpec.java`:
  ```java
  private static final PSSParameterSpec DEFAULT_SPEC =
      new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"),
                           32, PSSParameterSpec.TRAILER_FIELD_BC);
  ...
  checkSpec(doc, new PSSParameterSpec("SHA-512", "MGF1",
      new MGF1ParameterSpec("SHA-384"), 32, 2));
  ```
- `test/jdk/java/security/Signature/SignatureGetInstance.java`（校验默认 PSS 参数）。
- JDK 源码 `PSSParameterSpec.java` 中常量定义：
  ```java
  public static final int TRAILER_FIELD_BC = 1;
  public static final PSSParameterSpec DEFAULT =
      new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20, TRAILER_FIELD_BC);
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 AlgorithmParameterSpec、含 6 个 getter/toString 方法。
- 黑盒（testBlackBox）：
  - TRAILER_FIELD_BC == 1。
  - DEFAULT 非空且摘要 SHA-1、MGF MGF1、盐长 20、trailer 1。
  - `int` 构造器：盐长精确、摘要 SHA-1、MGF MGF1、trailer 为 TRAILER_FIELD_BC。
  - 5 参构造器：5 个 getter 精确回读，getMGFParameters 返回传入对象，toString 非空。
  - `instanceof AlgorithmParameterSpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：DEFAULT/int 构造默认矩阵（含 mgfParameters==SHA1 单例同引用）
- 关键实测：TRAILER_FIELD_BC==1；DEFAULT=（SHA-1,MGF1,MGF1ParameterSpec.SHA1 同引用,20,1）；int 构造=（SHA-1,MGF1,SHA1 单例,saltLen,1）；5 参构造校验：null digest/mgf→NPE、saltLen<0→IAE、trailerField<0→IAE（非 1 值如 2 不拒绝）、null mgfSpec 合法；toString 双侧格式不同（JDK-8 多行 MD:/SaltLength:，JDK-9+ PSSParameterSpec[...]）。
Signature.setParameter 用于 SHA256withRSA 等的 PSS 变体。
