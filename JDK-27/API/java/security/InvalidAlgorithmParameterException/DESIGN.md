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


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（2864672297499471472，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 三条真实路径消息不同（"Inappropriate parameter" / null spec / "Params must be instance of ..."），统一只断异常类型与消息非空，避免绑定实现文本。
