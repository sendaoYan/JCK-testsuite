# javax.security.sasl.RealmCallback 测试设计

## 1. API 功能
SASL 领域（realm）文本输入回调，继承 `javax.security.auth.callback.TextInputCallback`（后者实现 `Callback` 与 `Serializable`）：
- 构造：`RealmCallback(String prompt)`、`RealmCallback(String prompt, String defaultText)`。
- 继承方法：`getPrompt()`、`getDefaultText()`、`setText(String)`、`getText()`。

## 2. Java 用户如何使用
SASL 客户端 `CallbackHandler.handle` 中填入用户选择的 realm：
```java
if (cb instanceof RealmCallback) {
    RealmCallback rcb = (RealmCallback) cb;
    String dflt = rcb.getDefaultText();
    rcb.setText(dflt != null ? dflt : readLine(rcb.getPrompt()));
}
```
典型链路：`new RealmCallback(prompt) → getPrompt/getDefaultText → setText → getText`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/ClientCallbackHandler.java`（第 93–100 行）：`RealmCallback rcb = (RealmCallback) callbacks[i]; String realm = rcb.getDefaultText(); ... rcb.setText(realm);`
- 构造约束依据源码 `src/java.base/share/classes/javax/security/auth/callback/TextInputCallback.java`：prompt 为空/空串或 defaultText 为 null/空串时抛 `IllegalArgumentException`；`inputText` 初始为 null（`getText()` 初始 null）。

## 4. 测试用例设计 + 自动化实现
黑盒（`RealmCallbackTest.testBlackBox()`）：单参构造 `getPrompt`、默认文本 null、初始 `getText()` null、`setText` 后取值；双参构造 `getPrompt/getDefaultText`；异常路径：null prompt、空 prompt、null default 均抛 `IllegalArgumentException`。
白盒（`testWhiteBox()`）：public、具体类、父类 `TextInputCallback`、可赋值给 `Callback` 与 `Serializable`、拥有 `(String)` 与 `(String,String)` 两个公共构造。
