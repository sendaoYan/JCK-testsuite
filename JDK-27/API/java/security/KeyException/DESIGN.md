# java.security.KeyException

## 1. API 功能
基础密钥异常，继承 `java.security.GeneralSecurityException`，是 `InvalidKeyException` 等具体密钥异常的父类。四个构造器：
- 无参 `KeyException()`
- `KeyException(String msg)`
- `KeyException(String message, Throwable cause)`（@since 1.5）
- `KeyException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
作为密钥相关异常的通用父类，多被具体子类（如 `InvalidKeyException`）替代；也可直接用 `catch (KeyException e)` 捕获一整类密钥问题。
```java
try {
    // key operation
} catch (KeyException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/com/sun/crypto/provider/ChaCha20Cipher.java:1179` — `throw new KeyException("Counter exhausted.  " + ...);`
- `test/jdk/javax/xml/crypto/dsig/KeySelectors.java:150` — `} catch (KeyException ke) {`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:59` — `test(new KeyException(cause), new KeyException(MSG, cause));`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `GeneralSecurityException`、无接口、4 个构造器、无声明方法。
