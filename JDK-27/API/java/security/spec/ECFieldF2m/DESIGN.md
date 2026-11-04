# java.security.spec.ECFieldF2m

## 1. API 功能
椭圆曲线特征 2 有限域。三种构造：`(int m)` 正规基（normal basis），`(int m, BigInteger rp)` 多项式基（按约化多项式系数位），`(int m, int[] ks)` 多项式基（按中间项次数）。`getMidTermsOfReductionPolynomial()` 返回克隆数组，`getReductionPolynomial()` 为正规基时返回 null。实现 `ECField`，重写 `equals/hashCode`。

## 2. Java 用户如何使用
用户构造域对象后传给 `EllipticCurve(ECField field, BigInteger a, BigInteger b)` 来定义二元域上的椭圆曲线参数。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:42,47`：
  ```java
  private static final int[] F2M_KS = { 1 };
  private static final ECFieldF2m F2M = new ECFieldF2m(F2M_M, F2M_KS);
  ```
- `src/java.base/share/classes/java/security/spec/EllipticCurve.java` 的 `checkValidity` 对 `ECFieldF2m` 做 `getM()` 与系数范围校验。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `ECField`，方法 `getFieldSize/getM/getReductionPolynomial/getMidTermsOfReductionPolynomial/equals/hashCode` 存在。
- `testBlackBox()`：正规基 `(5)` 校验字段大小/m 且多项式相关为 null；`(5,{1})` 校验 rp=35 与中间项 {1}；`(5,35)` 校验 rp 与中间项；等价构造 `equals/hashCode` 一致、不同 m/中间项不等；`getMidTermsOfReductionPolynomial` 返回副本（变异后重取不变）；断言 `instanceof ECField`。
