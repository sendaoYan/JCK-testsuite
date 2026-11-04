# javax.swing.text.html.StyleSheet.BoxPainter — DESIGN

## 1. API 功能
盒子绘制器（`StyleSheet` 的嵌套 public static final 类，`extends Object implements Serializable`，非抽象）。核心方法分组：
- 工厂：由 `StyleSheet.getBoxPainter(AttributeSet)` 创建（构造器为包私有）。
- 用途：依据盒模型属性（margin/padding/border）绘制元素背景与边框。

## 2. Java 用户如何使用
`StyleSheet ss = new StyleSheet(); StyleSheet.BoxPainter bp = ss.getBoxPainter(attr);` 获得绘制器后交给 View 绘制；每次调用返回独立实例。典型链：`ss.getBoxPainter(ss.getDeclaration("margin-top: 10px"))`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:985`：`getBoxPainter(AttributeSet)` 返回 `new BoxPainter(a, css, this)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/BlockView.java:407`：`painter = sheet.getBoxPainter(attr);` 块视图获取绘制器。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ParagraphView.java:102`、`TableView.java:233`：同样调用 getBoxPainter。

## 4. 测试用例设计（黑盒部分，`BoxPainterTest.testBlackBox`）
1. `ss.getBoxPainter(空 AttributeSet)` 非 null 且 instanceof BoxPainter。
2. `ss.getBoxPainter(getDeclaration("margin-top: 10px; padding: 2px"))` 非 null。
3. 两次调用返回不同实例（bp2 != bp）。
