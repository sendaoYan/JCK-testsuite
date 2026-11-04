# java.security.spec.ECField

## 1. API 功能
椭圆曲线有限域的抽象接口，只声明 `int getFieldSize()`。两个实现：素数域 `ECFieldFp` 和特征 2 域 `ECFieldF2m`。

## 2. Java 用户如何使用
用户通过 `EllipticCurve.getField()` 取得 `ECField`，再按实际类型转型为 `ECFieldFp`/`ECFieldF2m` 后读取域参数。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/spec/EllipticCurve.java:44`：
  ```java
  private final ECField field;
  ```
- 同上 `EllipticCurve.java:50-55`：
  ```java
  private static void checkValidity(ECField field, BigInteger c, String cName) {
      if (field instanceof ECFieldFp) {
          BigInteger p = ((ECFieldFp)field).getP();
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、`isInterface()`、无父类，方法 `getFieldSize` 存在。
- `testBlackBox()`：断言 `ECFieldFp` 实例 `instanceof ECField`，且其 `getFieldSize()` 返回值正确（p=23 时 bitLength=5），验证接口契约。
