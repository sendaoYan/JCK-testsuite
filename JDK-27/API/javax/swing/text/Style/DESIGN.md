# javax.swing.text.Style — DESIGN

## 1. API 功能
命名样式的接口（`extends MutableAttributeSet`）。核心方法：
- `getName()`：样式名（可为 null）。
- `addChangeListener(ChangeListener)`、`removeChangeListener(ChangeListener)`：样式变化监听。

## 2. Java 用户如何使用
```java
Style s = context.addStyle("normal", parent);
s.addAttribute(StyleConstants.Bold, Boolean.TRUE);   // 继承 MutableAttributeSet
s.addChangeListener(e -> refresh());
String n = s.getName();
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`addStyle(String nm, Style parent)` 返回 `Style`，`getStyle(String)` 返回 `Style`；`NamedStyle implements Style`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/bug4148489.java`：`Style sty = sc.addStyle("normal", sc.getStyle(StyleContext.DEFAULT_STYLE)); StyleConstants.setFirstLineIndent(sty, -50);`。

## 4. 测试用例设计 + 自动化实现（`StyleTest.testBlackBox`）
基于 `StyleContext`：
1. 命名样式 getName、名称作为属性存储（初始 count==1）。
2. 样式作为 MutableAttributeSet 的 add/get。
3. ChangeListener 在 addAttribute 时触发一次、remove 后不再触发。
4. 未命名样式 getName 为 null。
5. 父子样式属性解析与本地 isDefined 语义。
