# javax.swing.text.StyleContext — DESIGN

## 1. API 功能
样式集合 + 属性集池的管理器（`implements Serializable, AbstractDocument.AttributeContext`）。核心方法：
- 常量：`DEFAULT_STYLE = "default"`。
- 工厂：`getDefaultStyleContext()`（单例）。
- 样式管理：`addStyle(String, Style)`、`getStyle(String)`、`removeStyle(String)`、`getStyleNames()`。
- 监听：`addChangeListener/removeChangeListener/getChangeListeners`。
- 字体/颜色解析：`getFont(AttributeSet)`、`getFont(String,int,int)`、`getFontMetrics`、`getForeground/getBackground`。
- AttributeContext 不可变集操作：`addAttribute/addAttributes/removeAttribute/removeAttributes/getEmptySet/reclaim`。
- 静态键注册表：`registerStaticAttributeKey/getStaticAttribute/getStaticAttributeKey`（配合序列化 writeAttributeSet/readAttributeSet）。

## 2. Java 用户如何使用
```java
StyleContext sc = new StyleContext();
Style base = sc.addStyle("base", sc.getStyle(StyleContext.DEFAULT_STYLE));
Style s = sc.getStyle("base");
Enumeration<?> names = sc.getStyleNames();
sc.removeStyle("base");
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：构造器 `addStyle(DEFAULT_STYLE, null)`；`addStyle` 内部 `new NamedStyle(nm, parent)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/bug4148489.java`：`sc = new StyleContext(); ... Style sty = sc.addStyle("normal", sc.getStyle(StyleContext.DEFAULT_STYLE));`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：静态块把 `StyleConstants.keys` 全部 `registerStaticAttributeKey`。

## 4. 测试用例设计 + 自动化实现（`StyleContextTest.testBlackBox`）
1. DEFAULT_STYLE 常量 + 默认样式预注册。
2. addStyle/getStyle 恒等、getStyleNames 排序后含 default/a/b、removeStyle、未知样式为 null。
3. getDefaultStyleContext 单例。
4. AttributeContext：addAttribute/removeAttribute 返回新集且源集不变。
5. getEmptySet；getFont(AttributeSet)/getFont(String,int,int) 解析 family/style/size。
6. 静态键注册表：getStaticAttributeKey 返回 String、getStaticAttribute 往返、未知键为 null。
