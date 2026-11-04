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


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（5698479920593359816，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 伪造密钥用静态命名类 `FakePrivKey implements PrivateKey`（匿名 Key 不能强转 PrivateKey，会先吃到 ClassCastException）；initVerify 需显式 `(PublicKey) null` 消除与 Certificate 重载的歧义。
