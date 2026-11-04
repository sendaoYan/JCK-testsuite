# java.security.InvalidParameterException

## 1. API 功能
JCA/JCE 引擎方法收到无效参数时抛出，继承 `java.lang.IllegalArgumentException`（非 GeneralSecurityException 体系）。四个构造器：
- 无参 `InvalidParameterException()`
- `InvalidParameterException(String msg)`
- `InvalidParameterException(String msg, Throwable cause)`（@since 20）
- `InvalidParameterException(Throwable cause)`（@since 20）

## 2. Java 用户如何使用
直接实例化抛出，或作为 `Provider`/`Security` 过滤非法参数时的运行时异常；属于 unchecked 异常，无需在签名中声明。
```java
if (param == null) {
    throw new InvalidParameterException("param must not be null");
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/Security.java:628` — `throw new InvalidParameterException("Invalid filter");`
- `src/java.base/share/classes/java/security/Provider.java:1895` — `throw new InvalidParameterException("supportsParameter() not ...");`
- `test/jdk/sun/security/tools/keytool/fakegen/java.base/sun/security/rsa/RSAKeyPairGenerator.java:60` — `throw new InvalidParameterException(iape);`（Throwable 构造器）
- `test/jdk/java/security/Exceptions/ChainingConstructors.java:58` — `test(new InvalidParameterException(cause), new InvalidParameterException(MSG, cause));`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof IllegalArgumentException` 且 `instanceof RuntimeException`。
白盒断言：public、具体类、父类 `IllegalArgumentException`、无接口、4 个构造器、无声明方法。


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（-857968536935667808，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- **版本差异**：JDK-8 仅 2 个公共构造器；JDK-9+ 起 4 个（补 cause 构造器）——两侧各自断言 getConstructors().length。
