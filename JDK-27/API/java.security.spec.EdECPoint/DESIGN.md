# java.security.spec.EdECPoint

## 1. API 功能
RFC 8032 Edwards 曲线点：由 y 坐标（`BigInteger`）和 x 坐标奇偶性（`boolean xOdd`）表示。`final` 类，`isXOdd()` 返回 x 奇偶标志，`getY()` 返回 y 坐标。

## 2. Java 用户如何使用
用户用 `new EdECPoint(xOdd, y)` 构造公钥点，再传给 `EdECPublicKeySpec`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/sun/security/ec/ed/EdDSAOperations.java:81-83`：
  ```java
  private static EdECPoint asEdECPoint(AffinePoint p) {
      return new EdECPoint(p.getX().asBigInteger().testBit(0),
          p.getY().asBigInteger());
  }
  ```
- `src/java.base/share/classes/java/security/interfaces/EdECPublicKey.java` 的 `getPoint()` 返回 `EdECPoint`。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、`final`、非接口、父类 `Object`，方法 `isXOdd/getY` 存在。
- `testBlackBox()`：`new EdECPoint(true, BigInteger.TEN)` 校验 `isXOdd==true` 与 `getY==10`；`new EdECPoint(false, BigInteger.ONE)` 校验 `isXOdd==false` 与 `getY==1`。
