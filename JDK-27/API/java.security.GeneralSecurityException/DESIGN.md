# java.security.GeneralSecurityException

## 1. API 功能
安全异常的通用基类（直接继承 `java.lang.Exception`），提供类型安全。四个构造器：
- 无参 `GeneralSecurityException()`
- `GeneralSecurityException(String msg)`
- `GeneralSecurityException(String message, Throwable cause)`（@since 1.5）
- `GeneralSecurityException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
通常作为 `catch (GeneralSecurityException e)` 统一捕获 JCA/JCE 引擎抛出的各类安全异常；构造时传入 detail message 描述原因，或包一层 cause 保留底层异常链。
```java
try {
    // ... JCA/JCE operation
} catch (GeneralSecurityException e) {
    String msg = e.getMessage();     // detail message
    Throwable c = e.getCause();      // underlying cause
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/javax/crypto/EncryptedPrivateKeyInfo.java:555` — `throw new GeneralSecurityException(e);`（Throwable 构造器包 cause）
- `src/java.base/share/classes/sun/security/ssl/RSAKeyExchange.java:242` — `throw new GeneralSecurityException(...);`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:53` — `test(new GeneralSecurityException(cause), new GeneralSecurityException(MSG, cause));`
- `test/jdk/java/security/PEM/PEMDecoderTest.java:169` — `} catch (GeneralSecurityException e) {`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器的消息/原因行为：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof Exception`。
白盒断言：public、具体类、父类 `java.lang.Exception`、无接口、4 个构造器、无声明方法。
