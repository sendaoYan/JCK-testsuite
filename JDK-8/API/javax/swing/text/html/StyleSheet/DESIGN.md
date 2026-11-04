# javax.swing.text.html.StyleSheet — DESIGN

## 1. API 功能
HTML CSS 样式表（`extends StyleContext`，非抽象，无接口）。核心方法分组：
- 规则/声明：`addRule(String)`、`getRule(String)`、`getDeclaration(String)`、`loadRules(...)`、`importStyleSheet(URL)`、`translateHTMLToCSS(AttributeSet)`。
- 颜色/字号：`stringToColor(String)`、static `getIndexOfSize(float)`、`getPointSize(int)`、`getPointSize(String)`。
- 级联/基础：`addStyleSheet/removeStyleSheet(StyleSheet)`、`getStyleSheets()`、`setBase/getBase`、`getViewAttributes`。
- 绘制器：`getBoxPainter(AttributeSet)`、`getListPainter(AttributeSet)`。

## 2. Java 用户如何使用
`StyleSheet ss = new StyleSheet(); ss.addRule("p { color: red; }"); ss.getRule("p")` 拿样式；`ss.stringToColor("red")` 转颜色；`ss.getPointSize(3)` 换算字号；`ss.addStyleSheet(sub)` 级联。典型链：addRule → getRule → isDefined(CSS.Attribute.COLOR)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`ss.addStyleSheet(styles);` 把默认样式表级联。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLDocument.java`：`getStyleSheet().getDeclaration(decl);` 解析内联 style。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/BlockView.java`：`painter = sheet.getBoxPainter(attr);`。

## 4. 测试用例设计（黑盒，`StyleSheetTest`：testShape/testDeclarationsAndRules/testPointSizes/testColors/testLinkage/testCssAttributes/testPaintersAndStyles，144 断言）
1. testDeclarationsAndRules：getDeclaration("color: red") 恰 1 属性定义 COLOR；组合声明多属性；getRule("p") 非 null 且定义 COLOR；getRule(未知 selector) 非 null；getRule(null) NPE（精确异常）。
2. testPointSizes：getIndexOfSize(12.0f)==3；getPointSize(3)==12.0f、(7)==36.0f、("3")==12.0f（固定换算表）。
3. testColors：stringToColor("red"/"#ff0000")==Color.red、"#F00"/"123" 按版本分支断言（JDK-8 与 JDK-27 解析差异）、未知 null。
4. testLinkage：setBase/getBase 往返；addStyleSheet 后 getStyleSheets() 长度 1 且身份相等；removeStyleSheet 后 null。
5. testCssAttributes：addCSSAttribute 组合、getViewAttributes、translateHTMLToCSS 往返。
6. testPaintersAndStyles：getBoxPainter/getListPainter 非 null 且身份稳定；style 增删与 removeStyle。
