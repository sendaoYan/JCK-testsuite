# java.security.spec.RSAMultiPrimePrivateCrtKeySpec 测试设计

## 1. API 功能
`RSAMultiPrimePrivateCrtKeySpec` 继承 `RSAPrivateKeySpec`，描述多素数 CRT 形式 RSA 私钥：模数、公钥指数、私钥指数、素数 p/q 及其 CRT 指数/系数，以及额外的 `RSAOtherPrimeInfo[]` 数组。提供 9 参与 10 参（附加 AlgorithmParameterSpec）构造器；getOtherPrimeInfo 返回数组的防御性拷贝。

## 2. Java 用户如何使用
```java
RSAMultiPrimePrivateCrtKeySpec spec = new RSAMultiPrimePrivateCrtKeySpec(
    modulus, publicExp, privateExp, p, q, expP, expQ, crtCoeff, otherPrimes);
RSAOtherPrimeInfo[] others = spec.getOtherPrimeInfo();  // 返回拷贝
```

## 3. 开源代码实际用法调研
- JDK 源码 `java.base/share/classes/java/security/interfaces/RSAMultiPrimePrivateCrtKey.java`（接口声明返回该规范使用的元素类型）:
  ```java
  RSAOtherPrimeInfo[] getOtherPrimeInfo();
  ```
- JDK 源码 `java.security.spec.RSAMultiPrimePrivateCrtKeySpec.java`（getOtherPrimeInfo 的防御性拷贝实现）:
  ```java
  public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
      if (otherPrimeInfo == null) return null;
      return otherPrimeInfo.clone();
  }
  ```
- 说明：多素数 RSA 已被 SunRsaSign 等主流 provider 移除，因此 test/jdk 中无直接构造实例的用例，本类主要用于 KeyFactory 的 getKeySpec 类型支持。

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 RSAPrivateKeySpec、含 7 个本类 getter 方法。
- 黑盒（testBlackBox）：
  - 9 参构造：8 个 BigInteger getter（含继承的 getModulus/getPrivateExponent）+ getParams 为 null 精确断言。
  - getOtherPrimeInfo 返回内容相等的新数组；修改返回数组不影响内部值。
  - 10 参构造：getParams 为传入对象、getOtherPrimeInfo 长度正确。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：全 getter 逐项往返
- 关键实测：8 个 BigInteger + RSAOtherPrimeInfo[]（+keyParams）；null 数组合法（getter null）、空数组→IAE "must not be empty"；ctor/getOtherPrimeInfo 双重防御拷贝。
多素数 RSA（PKCS#1 v2.1）私钥材料交换。
