# javax.swing.text.html.HTMLEditorKit.HTMLFactory — DESIGN

## 1. API 功能
`HTMLEditorKit` 的共享 `ViewFactory` 实现（implements `ViewFactory`），公开无参构造器 + `public View create(Element elem)`：按元素 `StyleConstants.NameAttribute` 的 `HTML.Tag` 映射到对应视图（CONTENT→`InlineView`、`p-implied`→`ParagraphView`、HTML→`BlockView(Y_AXIS)`、BODY→`BodyBlockView`、TABLE→`TableView`、IMG→`ImageView` 等；未知则回落 `LabelView`）。`HTMLEditorKit.getViewFactory()` 返回静态共享实例 `defaultFactory`。

## 2. Java 用户如何使用
一般通过 `HTMLEditorKit.getViewFactory()` 取得共享工厂；需要自定义时 `new HTMLEditorKit.HTMLFactory()`，对文档元素（`doc.getParagraphElement(pos)`/`doc.getCharacterElement(pos)`/`doc.getDefaultRootElement()`）调用 `create(elem)` 得到 `View`。构造视图本身无需显示（headless 安全），仅在绘制时依赖图形环境。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`private static final ViewFactory defaultFactory = new HTMLFactory();` 与 `public ViewFactory getViewFactory() { return defaultFactory; }`；`create(Element)` 内 `if (kind == HTML.Tag.CONTENT) return new InlineView(elem); ... else if (kind == HTML.Tag.HTML) return new BlockView(elem, View.Y_AXIS);`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/View.java` 等视图体系：`ViewFactory.create` 是 `BasicTextUI`/`View.getView` 建立视图树的入口，HTMLFactory 是其 HTML 特化实现。

## 4. 测试用例设计（黑盒部分，`HTMLFactoryTest.testBlackBox`）
1. `new HTMLFactory()` 可实例化；`kit.getViewFactory()` 是 HTMLFactory 实例。
2. 构造文档并 read 一段 HTML 后，`create(paragraph)` 返回 `ParagraphView`。
3. `create(characterElement)` 返回 `InlineView`。
4. `create(defaultRootElement)` 返回非空 View（BlockView 分支）。
