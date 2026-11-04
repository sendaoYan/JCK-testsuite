# javax.swing.event.HyperlinkListener 测试设计

## 1. API 功能

`HyperlinkListener` 监听超链接相关事件，继承 `java.util.EventListener`，一个回调：

- `void hyperlinkUpdate(HyperlinkEvent e)`：超链接进入/退出/激活时触发。

配套事件 `HyperlinkEvent`（继承 `java.util.EventObject`）携带：`getEventType()`（`EventType.ENTERED/EXITED/ACTIVATED`）、`getDescription()`、`getURL()`、`getSourceElement()`（`javax.swing.text.Element`）、`getInputEvent()`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JEditorPane`：

```java
JEditorPane pane = new JEditorPane();
HyperlinkListener l = new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            // 打开 e.getURL()
        }
    }
};
pane.addHyperlinkListener(l);
```

事件对象由 `JEditorPane` 内部构造（带 `URL`、`Element`、`InputEvent`）。测试时也可手工 `new HyperlinkEvent(source, type, url, desc, element, inputEvent)`。

典型组合链路：`addHyperlinkListener → 鼠标移入链接 → hyperlinkUpdate(ENTERED) → 点击 → hyperlinkUpdate(ACTIVATED) → 读 e.getURL() 打开页面`。三种 `EventType` 区分进入/离开/激活。

## 3. 开源代码实际用法调研

1. JDK `JEditorPane` 文档给出的标准监听器实现（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JEditorPane.java:162`）：
   ```java
   class Hyperactive implements HyperlinkListener {
       public void hyperlinkUpdate(HyperlinkEvent e) {
           if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
               JEditorPane pane = (JEditorPane) e.getSource();
               ...
               pane.setPage(e.getURL());
           }
       }
   }
   ```
2. `JEditorPane.addHyperlinkListener`（同文件 `:356`）把监听器存入 `listenerList`，`getHyperlinkListeners()` 返回数组。
3. `JEditorPane.fireHyperlinkUpdate`（同文件 `:399` 附近）遍历 `HyperlinkListener` 并调用 `hyperlinkUpdate`，是回调分发点。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`HyperlinkListenerTest.testBlackBox()`）：
- 匿名实现记录事件对象。
- 用 6 参构造器构造 `HyperlinkEvent`（含 `URL`、`PlainDocument` 的 `Element`、`MouseEvent` 作为 `InputEvent`），调用 `hyperlinkUpdate`。
- 断言回调触发、`getSource/getEventType/getURL/getDescription/getSourceElement/getInputEvent` 全部 round-trip。
- 分别以 `ENTERED/EXITED` 类型再触发，断言 `getEventType()` 正确，并验证三个常量互异、`ACTIVATED.toString()` 正确。

白盒保留原有结构断言，新增 `hyperlinkUpdate(HyperlinkEvent)` 返回 void/抽象与 `declared method count == 1`。
