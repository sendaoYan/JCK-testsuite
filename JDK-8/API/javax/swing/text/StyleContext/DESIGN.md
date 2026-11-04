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
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/StyleContext.java`：构造器 `addStyle(DEFAULT_STYLE, null)`；`addStyle` 内部 `new NamedStyle(nm, parent)`。
- `~/git/jdk8u/jdk/test/javax/swing/text/bug4148489.java`：`sc = new StyleContext(); ... Style sty = sc.addStyle("normal", sc.getStyle(StyleContext.DEFAULT_STYLE));`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/StyleContext.java`：静态块把 `StyleConstants.keys` 全部 `registerStaticAttributeKey`。

## 4. 测试用例设计 + 自动化实现
`testDefaultContext/testStyleTable/testChangeEvents/testAttributeContext/testFontsAndColors/testNamedStyleBehavior/testStaticAttributeKeys/testAttributeSetStream/testSerialization`（74 断言）：
- 默认上下文单例语义；DEFAULT_STYLE 名 "default"；新上下文恰含默认 style；getStyle 未注册返回 null、getStyle(null) 抛 NPE。
- style 表：addStyle 返回 NamedStyle 并注册（匿名 style 名 null 且不注册）、parent 记录并可解析、removeStyle 后 getStyle null 且其它 style 保留。
- 上下文 change 事件：addStyle/removeStyle 各触发一次，style 自身属性变化不触发上下文事件。
- AttributeContext 契约：getEmptyAttributeSet 返回 SimpleAttributeSet.EMPTY 且稳定；addAttribute/addAttributes/removeAttribute/removeAttributes 返回的集合对同参数幂等 interning；写操作不改原集合（不可变更新）。
- 字体派生：getFont 同 (family,style,size) 缓存 identity；superscript/subscript 使 size 减 2；getForeground/getBackground 默认 black、可由属性覆盖。
- NamedStyle 行为：每次属性变化恰好一个 ChangeEvent；copyAttributes 返回仍是 NamedStyle 且与源独立。
- 静态键注册表：readAttributes/writeAttributes 往返已注册键与自定义键；unregistered/null 键查找返回 null。
- 序列化：StyleContext round-trip 后 style 仍在、字体属性/粗体/名字精确保留。
