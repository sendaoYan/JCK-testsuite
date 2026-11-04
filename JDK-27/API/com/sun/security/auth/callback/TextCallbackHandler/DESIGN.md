# com.sun.security.auth.callback.TextCallbackHandler 测试设计案

## 1. API 功能
`TextCallbackHandler` 实现 `javax.security.auth.callback.CallbackHandler`。JDK-27 javap 实测**只有无参构造器**（brief 中提到的带 InputStream/PrintStream 构造器不存在）：
- 构造器 `TextCallbackHandler()`。
- `void handle(Callback[]) throws IOException, UnsupportedCallbackException`：委托给内部 `sun.security.util.ConsoleCallbackHandler`，处理 `TextOutputCallback`（写 stderr）、`NameCallback/PasswordCallback/ConfirmationCallback`（读 stdin），其它类型抛 `UnsupportedCallbackException`。

## 2. Java 用户如何使用
```java
CallbackHandler h = new TextCallbackHandler();
h.handle(new Callback[]{ new TextOutputCallback(TextOutputCallback.INFORMATION, "msg") });
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/callback/TextCallbackHandler.java` 与 `java.base/.../sun/security/util/ConsoleCallbackHandler.java`。
- JDK 回归：`~/git/jdk25u-dev/test/jdk/com/sun/security/auth/callback/TextCallbackHandler/{Default,Password,Confirm,NPE}.java`。
（网络不可用，以 JDK 源码 + test/jdk 为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `TextCallbackHandlerTest.java` 的 `testBlackBox()`：
- 空数组 `handle(new Callback[0])` 不抛。
- `TextOutputCallback`（INFORMATION/WARNING/ERROR）组合不抛且不读控制台输入。
- `LanguageCallback` 抛 `UnsupportedCallbackException`。
- 两个独立 handler 各自处理不共享状态。
（刻意避免 `NameCallback/PasswordCallback`，因为它们会阻塞读 `System.in`，jtreg 下无控制台。）
