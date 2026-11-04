# DestroyFailedException 设计案

## 1. API 功能
`javax.security.auth.DestroyFailedException` 继承 `java.lang.Exception`，表示 `Destroyable.destroy()` 失败。
- 构造器：`DestroyFailedException()`（无消息）、`DestroyFailedException(String msg)`。
- 继承自 `Throwable`：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/DestroyFailedException.java`。

## 2. Java 用户如何使用
```java
try { cred.destroy(); }
catch (DestroyFailedException e) {
    e.getMessage(); // 诊断信息
    Throwable c = e.getCause(); // 如实现方通过 initCause 附加底层原因
}
```
典型链路：捕获 `destroy()` 异常 → 读取 `getMessage` → 通过 `initCause/getCause` 传递底层失败原因。

## 3. 开源代码实际用法调研
- JDK 源码：两个构造器仅 `super()` / `super(msg)`，无额外逻辑。
- JDK 自带测试 `test/jdk/javax/security/auth/Destroyable/KeyDestructionTest.java` 覆盖 `Destroyable` 与
  `DestroyFailedException` 在 `Subject` 凭据销毁中的配合（本测试调研了该目录，未逐行依赖）。
- 异常无 `(String, Throwable)` 构造器，用户自定义 cause 需用 `initCause`（JDK 源码确认）。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`DestroyFailedExceptionTest.java` 的 `testBlackBox()`）：
- 消息构造：`new DestroyFailedException("msg").getMessage()` 为 `"msg"`。
- 无参构造：`getMessage()` 为 null。
- 原因链：`initCause` 返回 this、`getCause` 返回设置的原因、`getStackTrace` 可调用。
白盒补齐：`public`、非 final、父类 `Exception`、存在无参与 `String` 构造器。
