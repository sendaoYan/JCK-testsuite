# javax.swing.text.html.StyleSheet.ListPainter — DESIGN

## 1. API 功能
列表绘制器（`StyleSheet` 的嵌套 public static final 类，`extends Object implements Serializable`，非抽象）。核心方法分组：
- 工厂：由 `StyleSheet.getListPainter(AttributeSet)` 创建（构造器为包私有）。
- 用途：依据 list-style 属性绘制列表项的项目符号/编号。

## 2. Java 用户如何使用
`StyleSheet ss = new StyleSheet(); StyleSheet.ListPainter lp = ss.getListPainter(attr);` 获得绘制器后交给 ListView 绘制列表标记；每次调用返回独立实例。典型链：`ss.getListPainter(ss.getDeclaration("list-style-type: square"))`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:996`：`getListPainter(AttributeSet)` 返回 `new ListPainter(a, this)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ListView.java:118`：`listPainter = getStyleSheet().getListPainter(getAttributes());` 列表视图获取绘制器。

## 4. 测试用例设计（黑盒部分，`ListPainterTest.testBlackBox`）
1. `ss.getListPainter(空 AttributeSet)` 非 null 且 instanceof ListPainter。
2. `ss.getListPainter(getDeclaration("list-style-type: square"))` 非 null。
3. 两次调用返回不同实例（lp2 != lp）。
