# java.security.NoSuchProviderException

## 1. API 功能
请求的安全 Provider 在环境中不可用时抛出，继承 `java.security.GeneralSecurityException`。仅两个构造器：
- 无参 `NoSuchProviderException()`
- `NoSuchProviderException(String msg)`

（无 String+Throwable / Throwable 构造器，需在测试中避免实例化。）

## 2. Java 用户如何使用
`Cipher.getInstance(alg, provider)`、`Signature.getInstance(alg, provider)` 等带 provider 参数的工厂方法在 provider 名无效时抛出；message 通常携带 provider 名。
```java
try {
    Cipher c = Cipher.getInstance("AES", "NoSuchProvider");
} catch (NoSuchProviderException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/javax/crypto/Cipher.java:664` — `throw new NoSuchProviderException("No such provider: " + ...);`
- `src/java.base/share/classes/javax/crypto/KDF.java:410` — `throw new NoSuchProviderException(msg);`
- `test/jdk/java/security/SignedObject/Chain.java:287` — `} catch (NoSuchProviderException nspe) {`
- `test/jdk/sun/security/pkcs11/PKCS11Test.java:156` — `throw new NoSuchProviderException("No PKCS11 provider available");`

## 4. 测试用例设计 + 自动化实现
黑盒断言仅覆盖两个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- 继承：`instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `GeneralSecurityException`、无接口、2 个构造器、无声明方法。
