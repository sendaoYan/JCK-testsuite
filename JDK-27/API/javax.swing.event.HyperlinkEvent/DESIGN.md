# HyperlinkEvent 行为测试设计

## 1. API 功能
`javax.swing.event.HyperlinkEvent` 是超链接事件，继承 `java.util.EventObject`，携带 URL、描述、源元素、输入事件。

- **构造器**：四组重载，从 `(Object, EventType, URL)` 逐步追加 `String description`、`Element sourceElement`、`InputEvent inputEvent`（缺失项默认 null）。
- **查询**：`getEventType()`、`getURL()`、`getDescription()`、`getSourceElement()`、`getInputEvent()`、继承的 `getSource()`。

## 2. Java 用户如何使用
`JEditorPane` 激活超链接时派发，用户实现 `HyperlinkListener`：

```java
pane.addHyperlinkListener(e -> {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        URL u = e.getURL();            // 目标 URL
        String desc = e.getDescription();
        Element el = e.getSourceElement();
        InputEvent ie = e.getInputEvent();  // 触发点击的鼠标/键盘事件
    }
});
```

每个方法典型调用：`getEventType()` 判断 ENTERED/EXITED/ACTIVATED → `getURL()` 打开目标 → `getDescription()` 显示链接文本 → `getSourceElement()` 定位文档元素 → `getInputEvent()` 读取触发输入。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:983/1013/1027`：
   ```java
   linkEvent = new HyperlinkEvent(editor, ...);
   HyperlinkEvent exit = new HyperlinkEvent(editor, HyperlinkEvent.EventType.EXITED, url);
   HyperlinkEvent entered = new HyperlinkEvent(editor, HyperlinkEvent.EventType.ENTERED, url);
   ```
   内部用不同构造器生成 ACTIVATED/EXITED/ENTERED 事件。
2. `test/jdk/javax/swing/JEditorPane/ScrollToReferenceTest/ScrollToReferenceTest.java:74`：
   ```java
   if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && ...)
   ```
   用户侧依据 `getEventType()` 分支处理。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：用 `new URL("https://openjdk.org/")`（仅解析不联网）覆盖 3/4/6 参数构造器，验证各 getter 的 round-trip，并确认缺失参数（description/sourceElement/inputEvent）默认为 null；6 参数构造器验证全部字段。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe、无网络。
