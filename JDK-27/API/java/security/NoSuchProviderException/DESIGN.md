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


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（8488111756688534474，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 仅 2 个构造器（无 cause 构造器，双侧一致）——构造数断言防回归；消息为小写 "no such provider: <名>"，只断 contains。
