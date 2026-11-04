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


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（7509989324975124438，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- verify 长度负例用 RSA-512（签名恰 64 字节）：断言消息 contains "expecting 64"（JDK-8 "Signature length not correct: got 10 but was expecting 64" / JDK-27 "Bad signature length: got 10 but was expecting 64"，取公共子串）。
