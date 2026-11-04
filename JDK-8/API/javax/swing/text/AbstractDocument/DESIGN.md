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

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:114`：`PlainDocument` 覆盖 `insertString`，先按 `filterNewlines` 属性过滤换行再 `super.insertString(...)`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:96`：`DefaultStyledDocument` 通过 `new GapContent(BUFFER_SIZE_DEFAULT)` 与 `StyleContext` 组合出内容存储。
- `jdk/test/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0, "this", null); Element root = doc.getDefaultRootElement(); doc.setCharacterAttributes(...)`。
- `jdk/test/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：依赖 `PlainDocument`/`Position`/`Segment` 的偏移与位置跟踪。

## 4. 测试用例设计 + 自动化实现
`testTypeShape/testProperties/testRoots/testReplace/testRenderAndLocks/testDocumentFilter/testUndoableEditEvents/testListenerIsolation/testBleMatrix`（97 断言）：
- 类型面：abstract、implements Document+Serializable；元素名常量精确串 ParagraphElementName="paragraph"/ContentElementName="content"/SectionElementName="section"/BidiElementName="bidi level"、ElementNameAttribute="$ename"。
- 属性面：getDocumentProperties 返回活 Hashtable（双向可见）；putProperty null 移除；setDocumentProperties 整体替换后旧键消失；getAsynchronousLoadPriority 默认 -1、负值钳到 -1、0 保留。
- 根元素：恰两个 root，root1 为 bidi root（branch，一个名为 "bidi level" 的 leaf，区间随内容）。
- replace 分解语义：等价 remove+insert；0/0/null 全 no-op；越界经 remove 腿抛 BLE(getLength()+1)。
- render/锁：可重入（读锁计数）、Runnable 异常传播但锁正常释放、后续 render 仍可用；dump 在 headless 下产出输出。
- DocumentFilter 拦截：insertString/remove/replace 全部路由，参数精确（ins:1:XY / rm:0:3 / rep:0:4:Z）；不调用 super 即拦截生效；移除 filter 后恢复直通。
- UndoableEditEvent：presentationName "addition"、isSignificant true、source 为文档；no-op 不发事件；removeUndoableEditListener 后静默。
- 监听器隔离：抛异常的监听器把异常带回调用线程但文档已更新；通知内再突变文档 → IllegalStateException；重复注册保留两份；getListeners 按类型返回。
- BLE 矩阵（版本差异锁定，JDK-8 侧）：无文档层预检——insertString(getLength()+1,"!") 被 GapContent 接受（where==length 合法），但多出的尾字符无法经 remove 触达（remove 越界抛 BLE(getLength()+1)）；replace(getLength()+1,0,"z") 的尾插腿同样被接受；负偏移 insertString → BLE(getLength()+1)、getText 越界 → BLE(请求值)。（JDK-27 在文档层预检 offs>getLength()，二者抛 BLE(getLength()) 拒绝。）
