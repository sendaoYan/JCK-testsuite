# javax.swing.text.html.TableView — DESIGN

## 1. API 功能
HTML 表格视图（包私有 `class TableView extends BoxView implements ViewFactory`），渲染 `<table>`：按行/列布局单元格。核心成员：
- 构造器：`TableView(Element)`（包私有）。
- 网格查询：`getRowCount()`、`getColumnCount()`、`getColumnSpan(int)`、`getRowSpan(int)`、`getMultiRowSpan(int,int)`。
- `ViewFactory.create(Element)`：`<tr>` → RowView、`<td>` → CellView。
- 嵌套类：`CellView`、`RowView`、`TableViewIterator`。
- span 查询依赖内部 size pool，未布局（未 paint/未 layout）时 `getColumnCount/getColumnSpan` 抛 NPE、`getRowCount/getRowSpan/getMultiRowSpan` 返回 0。

## 2. Java 用户如何使用
用户不直接实例化：`HTMLEditorKit$HTMLFactory` 为 `<table>` 元素创建 TableView，行/单元格由其自身作为 ViewFactory 生成。测试需反射构造（`getConstructor(Element.class).setAccessible(true)` + `--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED`），且 span 查询对调用顺序敏感（任何前置布局初始化都会改变 NPE 行为）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/TableView.java`：`getRowCount/getColumnCount` 从 size pool（`rows`/`columns` 状态）读取；未初始化时 NPE。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/TableView/bug4412522.java`：表格布局回归。

## 4. 测试用例设计（黑盒，`TableViewTest`，19 断言，`@run main/othervm --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED`）
1. 元素树：`<table>` 下 2 个 `<tr>`、每个 `<tr>` 2 个 `<td>`（2x2 网格编码进元素树）；tr/td 的 parentElement 身份。
2. shape：类包私有、superclass BoxView、implements ViewFactory；嵌套 CellView/RowView 存在。
3. 反射契约（固定顺序，防 size pool 污染）：ctor.newInstance(table) → element 身份 → setParent(null) → getRowCount==0 → getColumnCount 抛 ITE(NPE) → getColumnSpan(0) 抛 ITE(NPE) → getRowSpan(0/9)==0 → getMultiRowSpan(0,1)==0。
4. factory：`create(td)` → CellView、`create(tr)` → RowView（类名 endsWith 断言）。
5. detach 后 getResizeWeight(X_AXIS)==0（中性零）。
