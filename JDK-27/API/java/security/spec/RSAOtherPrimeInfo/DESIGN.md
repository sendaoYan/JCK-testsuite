# java.security.spec.RSAOtherPrimeInfo 测试设计

## 1. API 功能
`RSAOtherPrimeInfo` 表示多素数 CRT RSA 私钥中除 p、q 之外的额外素数项，封装素数 `prime`、其指数 `exponent` 与 CRT 系数 `crtCoefficient`。构造器接收三个 BigInteger，getPrime/getExponent/getCrtCoefficient 为 final 访问器。

## 2. Java 用户如何使用
```java
RSAOtherPrimeInfo info = new RSAOtherPrimeInfo(prime, exponent, crtCoefficient);
BigInteger p = info.getPrime();
BigInteger e = info.getExponent();
BigInteger c = info.getCrtCoefficient();
```
通常作为 `RSAMultiPrimePrivateCrtKeySpec` 的 `otherPrimeInfo` 数组元素使用。

## 3. 开源代码实际用法调研
- JDK 源码 `java.base/share/classes/java/security/interfaces/RSAMultiPrimePrivateCrtKey.java`（接口方法返回该类型的数组）:
  ```java
  RSAOtherPrimeInfo[] getOtherPrimeInfo();
  ```
- JDK 源码 `java.security.spec.RSAMultiPrimePrivateCrtKeySpec.java`（作为构造参数与返回类型）:
  ```java
  private final RSAOtherPrimeInfo[] otherPrimeInfo;
  ...
  public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus, ..., RSAOtherPrimeInfo[] otherPrimeInfo)
  ```
- JDK 源码 `java.security.spec.RSAOtherPrimeInfo.java` 中 final 访问器：
  ```java
  public final BigInteger getPrime() { return this.prime; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 Object、无接口、含 getPrime/getExponent/getCrtCoefficient 方法。
- 黑盒（testBlackBox）：构造后三个 getter 分别精确返回传入的 BigInteger。
