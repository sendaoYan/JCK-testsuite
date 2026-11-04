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
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/StyleContext.java`：`addStyle(String nm, Style parent)` 返回 `Style`，`getStyle(String)` 返回 `Style`；`NamedStyle implements Style`。
- `~/git/jdk8u/jdk/test/javax/swing/text/bug4148489.java`：`Style sty = sc.addStyle("normal", sc.getStyle(StyleContext.DEFAULT_STYLE)); StyleConstants.setFirstLineIndent(sty, -50);`。

## 4. 测试用例设计 + 自动化实现
`testTypeShape/testNameContract/testChangeListenerContract/testStyleHierarchy`（26 断言）：
- 接口形状：extends MutableAttributeSet，仅新增 getName/addChangeListener/removeChangeListener。
- 名称契约：NamedStyle 名字存于 NameAttribute 键（setName 等价改该属性），getName 反向读取；匿名 style 名为 null 且无该键。
- 事件契约：属性变化（含 addAttributes/removeAttribute/setName）恰好触发一次 ChangeEvent，source 为 style 本身；removeChangeListener 后不再触发。
- 层级契约：addStyle 记录的 parent 通过 resolve parent 生效（值可穿透、本地可遮蔽、父集不受影响）；断开父链后本地属性保留。
