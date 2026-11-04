# java.security.spec.ECParameterSpec

## 1. API 功能
持有椭圆曲线域参数：曲线 `EllipticCurve curve`、基点 `ECPoint generator`、阶 `BigInteger order`、余因子 `int cofactor`。实现 `AlgorithmParameterSpec`。

## 2. Java 用户如何使用
用户显式构造这四个参数（或通过 `AlgorithmParameters` 从命名曲线解析），传给 `KeyPairGenerator.initialize(...)` 生成 EC 密钥对。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:53-54`：
  ```java
  private static final ECParameterSpec PARAMS = new ECParameterSpec
      (CURVE, POINT, ORDER, COFACTOR);
  ```
- `test/jdk/java/security/spec/IsAssignableFromOrder.java:75`：
  ```java
  testAlgSpec("EC", CurveDB.lookup("secp256r1"),
          ECParameterSpec.class, AlgorithmParameterSpec.class);
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `AlgorithmParameterSpec`，方法 `getCurve/getGenerator/getOrder/getCofactor` 存在。
- `testBlackBox()`：用素数域 p=23 构造曲线与基点，校验四个 getter 返回传入对象；断言 `instanceof AlgorithmParameterSpec`。
