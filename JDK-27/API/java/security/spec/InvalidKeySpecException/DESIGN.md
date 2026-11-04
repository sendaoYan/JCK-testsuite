# java.security.spec.InvalidKeySpecException 测试设计

## 1. API 功能
`InvalidKeySpecException` 是检查异常，继承 `GeneralSecurityException`，表示向算法工厂（如 KeyFactory）传入非法或不支持的密钥规范（KeySpec）时抛出。提供 4 个构造器：无参、`String`、`String+Throwable`、`Throwable`，分别用于设置消息与原因链。

## 2. Java 用户如何使用
```java
try {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    kf.generatePublic(new DSAPublicKeySpec(y, p, q, g));
} catch (InvalidKeySpecException e) {
    throw new InvalidKeySpecException("unsupported key spec", e);
}
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/KeyFactoryGetKeySpecForInvalidSpec.java`（捕获并校验该异常）:
  ```java
  } catch (InvalidKeySpecException e) {
      // Expected
  }
  ```
- JDK 源码 `src/java.base/share/classes/java/security/KeyFactory.java`（工厂内部抛出）:
  ```java
  throw new InvalidKeySpecException("Unknown key spec or inappropriate key spec");
  ```
- JDK 源码 `src/java.base/share/classes/com/sun/crypto/provider/DHKeyFactory.java` 中同样抛出该异常。

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 GeneralSecurityException。
- 黑盒（testBlackBox）：
  - 无参构造 getMessage 为 null。
  - `String` 构造 getMessage 精确匹配、getCause 为 null。
  - `String+Throwable` 构造 getMessage 与 getCause 精确匹配。
  - `Throwable` 构造 getCause 精确匹配、getMessage 非 null。
  - `instanceof GeneralSecurityException`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：构造矩阵（(Throwable) message==cause.toString()）
- 关键实测：4 构造（()/String/Throwable/String+Throwable），继承 GeneralSecurityException，Serializable。
KeyFactory.generatePublic/generatePrivate/getKeySpec 在规范无效时抛出。
