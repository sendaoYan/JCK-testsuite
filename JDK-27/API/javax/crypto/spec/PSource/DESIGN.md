# javax.crypto.spec.PSource 设计案

## 1. API 功能
OAEP 填充编码输入 P 的来源规范（PKCS#1 v2.2 / RFC 8017，since 1.5）：
- 构造：`protected PSource(String pSrcName)`（null → NPE）；不可直接实例化，必须子类化。
- 查询：`getAlgorithm()` 返回 PSource 算法名。
- 嵌套类型：`PSource.PSpecified`（public static final，算法名固定 "PSpecified"）——编码输入 P 的显式取值来源，见独立设计案。

## 2. Java 用户如何使用
```java
PSource pSrc = new PSource("MyPSource") { };        // 匿名子类（唯一实例化途径）
pSrc.getAlgorithm();                                 // -> "MyPSource"
// 标准用法：几乎只用 PSpecified 子类作为 OAEPParameterSpec 的 pSrc 参数
OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
        MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
c.init(Cipher.ENCRYPT_MODE, pubKey, spec);
```
典型链路：构造 PSource/PSpecified → 组进 `OAEPParameterSpec` → `Cipher.init` → RSA-OAEP 加解密往返。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/Cipher/RSA/TestOAEPParameterSpec.java`（L43-46）：`new OAEPParameterSpec(mdName, "MGF1", mgfSpec, new PSource.PSpecified(p))` 后送 `AlgorithmParameters.getInstance("OAEP")`/Cipher——真实链路以此为准。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/PSource.java`（L65-70）：protected ctor 校验 null 抛 NPE；PSpecified 固定名 "PSpecified"。
- JDK 自带测试 `test/jdk/com/sun/crypto/provider/AlgorithmParameters/OAEPOrder.java`：OAEP 参数编解码中 PSource 的顺序稳定性。
- Web 调研：开源项目中 PSource 几乎只以 `PSource.PSpecified.DEFAULT` 或 `new PSource.PSpecified(label)` 形式出现在 RSA-OAEP 工具类中（自定义 PSource 子类极罕见）。

## 4. 测试用例设计 + 自动化实现
`PSourceTest.java`：
- **白盒**：public/非 abstract/非 final、父类 Object、无直接接口、无公共构造器、唯一 protected(String) 构造器、public String getAlgorithm()、嵌套类 PSpecified（public static final，extends PSource，声明类正确）。
- **黑盒**：
  1. 匿名子类 getAlgorithm（保留原有 3 断言）；
  2. 重复调用稳定、不同子类名字互异、未覆写 equals（identity 语义）；
  3. PSpecified.DEFAULT 经继承方法返回 "PSpecified"；
  4. protected ctor null → NPE；
  5. 真实链路：PSpecified.DEFAULT 与带 label 的 PSpecified 分别组入 OAEPParameterSpec 完成 RSA-2048 OAEPWithSHA-256 往返。
