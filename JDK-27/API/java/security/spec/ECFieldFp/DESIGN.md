# java.security.spec.ECFieldFp

## 1. API 功能
椭圆曲线素数域，构造参数为素数 `p`。`getFieldSize()` 返回 `p.bitLength()`，`getP()` 返回素数本身。实现 `ECField`，重写 `equals/hashCode`（按 `p` 比较）。

## 2. Java 用户如何使用
用户构造 `new ECFieldFp(p)`，配合系数 `a,b` 传入 `new EllipticCurve(field, a, b)` 定义素数域椭圆曲线。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:39,49`：
  ```java
  private static final ECFieldFp FP = new ECFieldFp(TEN);
  ...
  private static final EllipticCurve CURVE = new EllipticCurve(new ECFieldFp(TEN), ONE, ONE);
  ```
- `src/java.base/share/classes/java/security/spec/EllipticCurve.java:53-54`：
  ```java
  BigInteger p = ((ECFieldFp)field).getP();
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `ECField`，方法 `getFieldSize/getP/equals/hashCode` 存在。
- `testBlackBox()`：`p=23` 校验 `getP`、`getFieldSize()==5`；相等实例 `equals` 且 `hashCode` 一致；`p=29` 不等、与非 `ECFieldFp` 对象不等；断言 `instanceof ECField`。
