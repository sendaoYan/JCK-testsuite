# java.security.spec.InvalidParameterSpecException 测试设计

## 1. API 功能
`InvalidParameterSpecException` 是检查异常，继承 `GeneralSecurityException`，表示算法参数（AlgorithmParameters）初始化状态非法或参数规范不匹配时抛出。提供两个构造器：无参与 `String`。

## 2. Java 用户如何使用
```java
try {
    AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
    params.init(new byte[]{...});   // 未指定格式，可能抛出
} catch (InvalidParameterSpecException e) {
    throw new InvalidParameterSpecException("bad parameter spec");
}
```

## 3. 开源代码实际用法调研
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java`（显式构造无参异常）:
  ```java
  InvalidParameterSpecException exc = new InvalidParameterSpecException();
  ```
- JDK 源码 `src/java.base/share/classes/java/security/AlgorithmParameters.java`（内部抛出带消息异常）:
  ```java
  throw new InvalidParameterSpecException("already initialized");
  ```
- JDK 源码 `src/java.base/share/classes/com/sun/crypto/provider/PBEParameters.java` 中同样抛出该异常。

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 GeneralSecurityException。
- 黑盒（testBlackBox）：无参构造 getMessage 为 null；`String` 构造 getMessage 精确匹配且 getCause 为 null；`instanceof GeneralSecurityException`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：构造器数==2 的契约断言
- 关键实测：仅 2 构造（()/String）——无 cause 构造器（与 InvalidKeySpecException 的关键差异）；继承 GeneralSecurityException，Serializable。
AlgorithmParameters.init/getParameterSpec 在规范不匹配时抛出。
