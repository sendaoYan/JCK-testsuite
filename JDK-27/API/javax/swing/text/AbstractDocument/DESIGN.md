# javax.swing.text.AbstractDocument — DESIGN

## 1. API 功能
`AbstractDocument` 是 `Document` 接口的抽象基类，实现了大部分公共契约，只把 `getDefaultRootElement()` 和 `getParagraphElement(int)` 留给子类。方法分组：
- 数据操作：`getLength()`、`getText(int,int)`、`getText(int,int,Segment)`、`insertString(int,String,AttributeSet)`、`remove(int,int)`、`replace(int,int,String,AttributeSet)`。
- 位置/结构：`createPosition(int)`、`getStartPosition()`、`getEndPosition()`、`getRootElements()`、`getDefaultRootElement()`、`getBidiRootElement()`、`getParagraphElement(int)`。
- 属性：`getDocumentProperties()`、`setDocumentProperties(Dictionary)`、`getProperty(Object)`、`putProperty(Object,Object)`。
- 监听器：`addDocumentListener`、`removeDocumentListener`、`getDocumentListeners`、`addUndoableEditListener`、`removeUndoableEditListener`、`getUndoableEditListeners`。
- 杂项：`render(Runnable)`、`setDocumentFilter`、`getDocumentFilter`、`getAsynchronousLoadPriority`、`setAsynchronousLoadPriority`、`dump(PrintStream)`。

## 2. Java 用户如何使用
用户不能直接 `new AbstractDocument`，而是 `new PlainDocument()`（或 `new DefaultStyledDocument()`）后调用继承的方法。典型组合链：
`doc.insertString(0, "Hello", null)` → `doc.getLength()` → `doc.getText(0, doc.getLength())` → `doc.remove(0, 5)` → `doc.createPosition(0)` 观察偏移跟踪 → `doc.putProperty("k","v")` → `doc.getProperty("k")` → `doc.addUndoableEditListener(...)` 捕获编辑并 `undo()/redo()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:114`：`PlainDocument` 覆盖 `insertString`，先按 `filterNewlines` 属性过滤换行再 `super.insertString(...)`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:96`：`DefaultStyledDocument` 通过 `new GapContent(BUFFER_SIZE_DEFAULT)` 与 `StyleContext` 组合出内容存储。
- `test/jdk/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0, "this", null); Element root = doc.getDefaultRootElement(); doc.setCharacterAttributes(...)`。
- `test/jdk/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：依赖 `PlainDocument`/`Position`/`Segment` 的偏移与位置跟踪。

## 4. 测试用例设计 + 自动化实现（`AbstractDocumentTest.testBlackBox`）
- 用 `PlainDocument` 实例走主链路：insertString/getLength/getText/remove/replace 往返。
- 元素树遍历：`getDefaultRootElement()`、`getParagraphElement(0)`、子元素区间。
- `createPosition` 偏移跟踪（在位置前插入则右移）、`getStartPosition/getEndPosition`。
- `getRootElements()` 返回 2 元素（默认根 + bidi 根）。
- 属性 `putProperty/getProperty`（含 null 删除）、`setDocumentProperties`。
- `getAsynchronousLoadPriority` 默认 -1 与往返、`setDocumentFilter/getDocumentFilter`。
- `render(Runnable)` 执行回调；`addDocumentListener/removeDocumentListener` 通知计数。
- `addUndoableEditListener` 捕获编辑后 `undo()/redo()` 反转/重放插入。
- 边界：`getText(0,-1)`、越界 `insertString/remove` 均抛 `BadLocationException`。

## 5. 契约深化（本轮追加）
`testTypeShape/testProperties/testRoots/testReplace/testRenderAndLocks/testDocumentFilter/testUndoableEditEvents/testListenerIsolation/testBleMatrix`（JDK-27 95 断言）：
- 类型面：abstract、implements Document+Serializable；元素名常量精确串 ParagraphElementName="paragraph"/ContentElementName="content"/SectionElementName="section"/BidiElementName="bidi level"、ElementNameAttribute="$ename"。
- 属性面：getDocumentProperties 返回活 Hashtable（双向可见）；putProperty null 移除；setDocumentProperties 整体替换后旧键消失；getAsynchronousLoadPriority 默认 -1、负值钳到 -1、0 保留。
- 根元素：恰两个 root，root1 为 bidi root（branch，一个名为 "bidi level" 的 leaf，区间随内容）。
- replace 分解语义：等价 remove+insert；0/0/null 全 no-op；越界经 remove 腿抛 BLE(getLength()+1)（双侧一致）。
- render/锁：可重入（读锁计数）、Runnable 异常传播但锁正常释放、后续 render 仍可用；dump 在 headless 下产出输出。
- DocumentFilter 拦截：insertString/remove/replace 全部路由，参数精确（ins:1:XY / rm:0:3 / rep:0:4:Z）；不调用 super 即拦截生效；移除 filter 后恢复直通。
- UndoableEditEvent：presentationName "addition"、isSignificant true、source 为文档；no-op 不发事件；removeUndoableEditListener 后静默。
- 监听器隔离：抛异常的监听器把异常带回调用线程但文档已更新；通知内再突变文档 → IllegalStateException（嵌套突变被拒）；重复注册保留两份；getListeners 按类型返回。
- BLE 矩阵（版本差异锁定）：JDK-27 在文档层预检 offs>getLength()，insertString(getLength()+1) 与 replace(getLength()+1,0,"z") 都抛 BLE(getLength())（JDK-8 因 GapContent 允许 where==length 而接受该插入）；负偏移 insertString/getText 越界的 offsetRequested 精确；零长度 remove 任意位置 no-op。
