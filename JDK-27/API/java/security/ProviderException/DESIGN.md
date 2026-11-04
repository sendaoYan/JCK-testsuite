# java.security.ProviderException

## 1. API 功能
Provider 的运行时异常（配置错误或不可恢复的内部错误），继承 `java.lang.RuntimeException`（非 GeneralSecurityException 体系）。四个构造器：
- 无参 `ProviderException()`
- `ProviderException(String s)`
- `ProviderException(String message, Throwable cause)`（@since 1.5）
- `ProviderException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
Provider 实现内部遇到不可恢复错误时抛出（unchecked），调用方通常不显式 catch，而是让它向上传播；message/cause 用于诊断 Provider 内部故障。
```java
throw new ProviderException("update() failed", e); // 包住底层异常
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/SignatureSpi.java:227` — `throw new ProviderException("update() failed", e);`（String+Throwable 构造器）
- `src/java.base/share/classes/com/sun/crypto/provider/GHASH.java:143` — `throw new ProviderException("Internal error");`
- `src/java.base/share/classes/com/sun/crypto/provider/HmacCore.java:239` — `throw new ProviderException(e);`（Throwable 构造器）
- `test/jdk/java/security/KeyPairGenerator/FinalizeHalf.java:86` — `} catch (ProviderException pe) {`

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof RuntimeException`。
白盒断言：public、具体类、父类 `RuntimeException`、无接口、4 个构造器、无声明方法。
