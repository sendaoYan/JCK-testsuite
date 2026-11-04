# HyperlinkEvent.EventType 行为测试设计

## 1. API 功能
`javax.swing.event.HyperlinkEvent$EventType` 是超链接事件类型值类（final class，非枚举），构造器私有，只暴露三个静态实例。

- **静态实例**：`ENTERED`、`EXITED`、`ACTIVATED`。
- **查询**：`toString()` 返回 `"ENTERED"`/`"EXITED"`/`"ACTIVATED"`。

## 2. Java 用户如何使用
作为 `HyperlinkEvent.getEventType()` 的返回值做身份比较：

```java
HyperlinkEvent.EventType t = e.getEventType();
if (t == HyperlinkEvent.EventType.ACTIVATED) { /* 打开链接 */ }
else if (t == HyperlinkEvent.EventType.ENTERED) { /* 显示提示 */ }
else if (t == HyperlinkEvent.EventType.EXITED) { /* 隐藏提示 */ }
```

每个方法典型调用：静态引用比较身份 → 需要日志/调试时 `toString()` 得到可读名称。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:1013/1027`：
   ```java
   new HyperlinkEvent(editor, HyperlinkEvent.EventType.EXITED, url);
   new HyperlinkEvent(editor, HyperlinkEvent.EventType.ENTERED, url);
   ```
   将三个静态实例作为事件类型参数传入。
2. `test/jdk/javax/swing/JEditorPane/ScrollToReferenceTest/ScrollToReferenceTest.java:74` 用 `HyperlinkEvent.EventType.ACTIVATED` 做身份比较。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 Object、非接口/注解/枚举、public、非 abstract），新增 `final` 修饰符断言。
- 黑盒：验证三个静态实例非空、`toString()` 精确值、两两身份互异、静态引用稳定性。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
