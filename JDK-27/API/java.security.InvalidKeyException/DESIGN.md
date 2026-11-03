# java.security.InvalidKeyException

## 1. API 功能
无效密钥（编码错误、长度错误、未初始化等）异常，继承 `java.security.KeyException`（后者继承 `GeneralSecurityException`）。四个构造器：
- 无参 `InvalidKeyException()`
- `InvalidKeyException(String msg)`
- `InvalidKeyException(String message, Throwable cause)`（@since 1.5）
- `InvalidKeyException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
`Signature.initVerify(PublicKey)`、`Cipher.init(...)` 等在密钥类型/用途不匹配时抛出；message 描述具体问题，cause 保留底层异常。
```java
try {
    sig.initVerify(publicKey);
} catch (InvalidKeyException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/Signature.java:526` — `throw new InvalidKeyException("Wrong key usage");`
- `src/java.base/share/classes/com/sun/crypto/provider/RSACipher.java:216` — `throw new InvalidKeyException("Wrong parameters", iape);`（String+Throwable 构造器）
- `test/jdk/java/security/Signature/SignatureGetInstance.java:176` — `throw new InvalidKeyException("Must be MyPubKey");`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:56` — `test(new InvalidKeyException(cause), new InvalidKeyException(MSG, cause));`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof KeyException` 且 `instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `KeyException`、无接口、4 个构造器、无声明方法。
