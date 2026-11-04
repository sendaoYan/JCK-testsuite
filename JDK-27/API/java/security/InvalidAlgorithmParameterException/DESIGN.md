# java.security.InvalidAlgorithmParameterException

## 1. API 功能
无效或不合适的算法参数异常，继承 `java.security.GeneralSecurityException`。四个构造器：
- 无参 `InvalidAlgorithmParameterException()`
- `InvalidAlgorithmParameterException(String msg)`
- `InvalidAlgorithmParameterException(String message, Throwable cause)`（@since 1.5）
- `InvalidAlgorithmParameterException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
`Signature.setParameter(...)`、`AlgorithmParameters.init(...)`、`CertPathBuilder.build(...)` 等在校验算法参数失败时抛出；cause 常为 `UnsupportedOperationException` 等底层异常。
```java
try {
    certPathBuilder.build(params);
} catch (InvalidAlgorithmParameterException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/SignatureSpi.java:96` — `throw new InvalidAlgorithmParameterException(usoe);`（Throwable 构造器）
- `src/java.base/share/classes/java/security/cert/PKIXParameters.java:202` — `throw new InvalidAlgorithmParameterException("the trustAnchors " + ...);`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:54-55` — `test(new InvalidAlgorithmParameterException(cause), new InvalidAlgorithmParameterException(MSG, cause));`
- `test/jdk/javax/crypto/KDF/KDFDelayedProviderException.java:94` — `throw new InvalidAlgorithmParameterException("Not Ed25519");`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `GeneralSecurityException`、无接口、4 个构造器、无声明方法。
