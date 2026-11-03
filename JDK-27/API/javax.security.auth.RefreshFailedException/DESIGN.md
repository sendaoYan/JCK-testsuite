# RefreshFailedException 设计案

## 1. API 功能
`javax.security.auth.RefreshFailedException` 继承 `java.lang.Exception`，表示 `Refreshable.refresh()` 失败。
- 构造器：`RefreshFailedException()`（无消息）、`RefreshFailedException(String msg)`。
- 继承自 `Throwable`：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/RefreshFailedException.java`。

## 2. Java 用户如何使用
```java
try { token.refresh(); }
catch (RefreshFailedException e) {
    e.getMessage(); // 诊断信息
    Throwable c = e.getCause(); // 底层原因（如通过 initCause 附加）
}
```
典型链路：捕获 `refresh()` 异常 → 读取 `getMessage` → `initCause/getCause` 传递原因。

## 3. 开源代码实际用法调研
- JDK 源码：两个构造器仅 `super()` / `super(msg)`，无额外逻辑。
- 异常无 `(String, Throwable)` 构造器，附加 cause 用 `Throwable.initCause`（JDK 源码确认）。
- JDK 自带测试 `test/jdk/javax/security/auth/` 下未发现独立的 `RefreshFailedException` 用例；本测试以 JDK 源码 + `Throwable` 契约为准。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`RefreshFailedExceptionTest.java` 的 `testBlackBox()`）：
- 消息构造：`getMessage()` 为 `"msg"`；无参构造 `getMessage()` 为 null。
- 原因链：`initCause` 返回 this、`getCause` 返回原因、`getStackTrace` 可调用。
白盒补齐：`public`、非 final、父类 `Exception`、存在无参与 `String` 构造器。
