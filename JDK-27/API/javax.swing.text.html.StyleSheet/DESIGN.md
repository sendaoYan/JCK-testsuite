# javax.swing.text.html.StyleSheet — DESIGN

## 1. API 功能
HTML CSS 样式表（`extends StyleContext`，非抽象，无接口）。核心方法分组：
- 构造器：`StyleSheet()`。
- 规则/声明：`addRule(String)`、`getRule(String)`、`getDeclaration(String)`。
- 颜色/字号：`stringToColor(String)`、`getIndexOfSize(float)`（static）、`getPointSize(int)`、`getPointSize(String)`。
- 级联/基础：`addStyleSheet(StyleSheet)`、`getStyleSheets()`、`removeStyleSheet(StyleSheet)`、`setBase(URL)`、`getBase()`。
- 绘制器工厂：`getBoxPainter(AttributeSet)`、`getListPainter(AttributeSet)`。

## 2. Java 用户如何使用
`StyleSheet ss = new StyleSheet();` 后：`ss.addRule("p { color: red; }")` → `ss.getRule("p")` 拿样式；`ss.getDeclaration("color: red")` 解析声明；`ss.stringToColor("red")` 转颜色；`ss.getPointSize(3)` 换算字号；`ss.addStyleSheet(sub)` 级联。典型链：addRule → getRule → isDefined(CSS.Attribute.COLOR)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:263`：`ss.addStyleSheet(styles);` 把默认样式表级联进去。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2846`：`styleAttributes = getStyleSheet().getDeclaration(decl);` 解析内联 style 声明。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:1061`：`stringToColor` 委托 `CSS.stringToColor`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/BlockView.java:407`：`painter = sheet.getBoxPainter(attr);` 取盒子绘制器。

## 4. 测试用例设计（黑盒部分，`StyleSheetTest.testBlackBox`）
1. `new StyleSheet()` 非 null。
2. getDeclaration("color: red")：属性数 1、定义 COLOR、未定义 FONT_FAMILY；"font-family: sans-serif" 定义 FONT_FAMILY；getDeclaration(null) 空。
3. addRule("p { color: red; }") → getRule("p") 非 null 且定义 COLOR、名 "p"；getRule("no-such-selector") 仍非 null；getRule(null) 抛 NPE。
4. stringToColor("red"/"#ff0000")==Color.red；未知颜色返回 null。
5. getIndexOfSize(12.0f)==3；getPointSize(3)==12.0f、getPointSize(7)==36.0f、getPointSize("3")==12.0f。
6. setBase/getBase 往返；addStyleSheet 后 getStyleSheets() 长度 1 且 ==sub；removeStyleSheet 后 getStyleSheets()==null。
