# java.security.SignatureException

## 1. API 功能
通用签名异常，继承 `java.security.GeneralSecurityException`。四个构造器：
- 无参 `SignatureException()`
- `SignatureException(String msg)`
- `SignatureException(String message, Throwable cause)`（@since 1.5）
- `SignatureException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
`Signature.sign()`/`verify()` 在对象未初始化、状态错误或签名失败时抛出；message 描述失败原因。
```java
try {
    boolean ok = sig.verify(sigBytes);
} catch (SignatureException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/Signature.java:686` — `throw new SignatureException("object not initialized for " + ...);`
- `src/java.base/share/classes/java/security/Signature.java:763` — `throw new SignatureException("object not initialized for " + ...);`
- `test/jdk/java/security/Signature/Offsets.java:187` — `} catch (SignatureException e) {`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:64` — `test(new SignatureException(cause), new SignatureException(MSG, cause));`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `GeneralSecurityException`、无接口、4 个构造器、无声明方法。
