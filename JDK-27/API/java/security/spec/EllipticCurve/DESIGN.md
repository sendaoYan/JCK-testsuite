# java.security.spec.EllipticCurve 测试设计

## 1. API 功能
`EllipticCurve` 表示定义在有限域 `ECField` 上的椭圆曲线 `y^2 = x^3 + ax + b`。它封装有限域、系数 a/b 以及可选的种子字节 seed。提供 getField/getA/getB/getSeed 访问器，以及基于 field、a、b 三者的 equals/hashCode（seed 不参与相等性判定）。seed 在构造与 getSeed 时都做防御性拷贝。

## 2. Java 用户如何使用
```java
ECField field = new ECFieldFp(new BigInteger("23"));
EllipticCurve c = new EllipticCurve(field, a, b);          // 无 seed
EllipticCurve c2 = new EllipticCurve(field, a, b, seed);   // 带 seed
ECField f = c.getField();
BigInteger a = c.getA();
byte[] seed = c.getSeed();  // 可能为 null，且每次返回新数组
```
曲线通常作为 `ECParameterSpec` 的组成部分，与生成点、阶、协因子一起描述椭圆曲线参数。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/EllipticCurveMatch.java`（测试获取曲线时附带 seed）:
  ```java
  return new EllipticCurve (curve.getField(), curve.getA(), curve.getB(), seed);
  ...
  return new EllipticCurve (field, a, b);
  ```
- `test/jdk/javax/xml/crypto/dsig/GenerationTests.java`（构造 P-256 曲线用于签名测试）。
- JDK 源码 `src/java.base/share/classes/java/security/spec/EllipticCurve.java` 中 `hashCode()`:
  ```java
  return (field.hashCode() << 6 + (a.hashCode() << 4) + (b.hashCode() << 2));
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 Object、无接口、含 getField/getA/getB/getSeed/equals/hashCode 方法。
- 黑盒（testBlackBox）：
  - 3 参构造：getField/getA/getB 回读、getSeed 为 null。
  - equals/hashCode：同 field/a/b 的两条曲线相等且哈希相同。
  - 4 参构造：getSeed 返回内容相等的新数组（防御性拷贝），修改入参数组不影响内部值；equals/hashCode 忽略 seed（同 field/a/b 不同 seed 仍相等）。
