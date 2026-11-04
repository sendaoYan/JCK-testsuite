# java.security.NoSuchAlgorithmException

## 1. API 功能
请求的加密算法在环境中不可用时抛出，继承 `java.security.GeneralSecurityException`。四个构造器：
- 无参 `NoSuchAlgorithmException()`
- `NoSuchAlgorithmException(String msg)`
- `NoSuchAlgorithmException(String message, Throwable cause)`（@since 1.5）
- `NoSuchAlgorithmException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
`MessageDigest.getInstance("SHA-1")` 等 `getInstance` 工厂方法在算法名不存在或被禁用时抛出；message 通常携带算法名。
```java
try {
    MessageDigest md = MessageDigest.getInstance("SHA-999");
} catch (NoSuchAlgorithmException e) {
    System.out.println("no such algorithm: " + e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/MessageDigest.java:190` — `throw new NoSuchAlgorithmException(algorithm + " is disabled");`
- `src/java.base/share/classes/java/security/AlgorithmParameters.java:172` — `throw new NoSuchAlgorithmException(algorithm + " not found");`
- `test/jdk/java/security/misc/TestDefaultRandom.java:179` — `throw new NoSuchAlgorithmException("No support for " + alg);`
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:62` — `test(new NoSuchAlgorithmException(cause), new NoSuchAlgorithmException(MSG, cause));`

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
- serialVersionUID 精确锁定（-7443947487218346562，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 五个引擎工厂（MessageDigest/Signature/KeyPairGenerator/KeyFactory/SecureRandom）bogus 名均断言 message contains 算法名（消息格式各引擎不同）。
