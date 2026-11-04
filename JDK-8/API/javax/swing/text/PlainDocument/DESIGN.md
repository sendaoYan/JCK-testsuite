# javax.swing.text.PlainDocument — DESIGN

## 1. API 功能
`AbstractDocument` 的纯文本实现，默认用 `GapContent` 存储，元素结构为按行映射。方法分组：
- 常量：`tabSizeAttribute`（"tabSize"）、`lineLimitAttribute`（"lineLimit"）。
- 构造：`PlainDocument()`、`PlainDocument(Content)`。
- 数据：`insertString(int,String,AttributeSet)`（支持 `filterNewlines` 属性过滤换行）。
- 结构：`getDefaultRootElement()`、`getParagraphElement(int)`。
- 保护钩子：`createDefaultRoot()`、`insertUpdate`、`removeUpdate`。

## 2. Java 用户如何使用
`new PlainDocument()` → `insertString(0,"one\ntwo",null)` → `getText(0,getLength())` → `getDefaultRootElement().getElementCount()` 得行数 → `getParagraphElement(pos)` 定位行；`putProperty("filterNewlines", Boolean.TRUE)` 后插入的 `\n` 被替换为空格。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/JTextField.java:379`：`createDefaultModel()` 返回 `new PlainDocument()`。
- `jdk/src/share/classes/javax/swing/JTextArea.java:260`：同样返回 `new PlainDocument()`。
- `jdk/src/share/classes/javax/swing/text/DefaultEditorKit.java:133`：`return new PlainDocument();`。
- `jdk/test/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：使用 `PlainDocument` 做偏移/位置跟踪。

## 4. 测试用例设计 + 自动化实现
`testTypeShape/testDefaults/testLineMap/testSplitAndMerge/testFilterNewlines/testProperties`（89 断言）：
- 类型面：直接父类 AbstractDocument、无直接接口（Document/Serializable 由父类继承）。
- 默认值：tabSize 属性 8、lineLimit 属性 null（模型层不强制）；空文档 root [0,1) 一个名为 "content" 的行 leaf。
- 行映射："ab\ncd\nef" → 三个行元素 [0,3)/[3,6)/[6,9)，名字 "content"、parent 是 root、无子、空属性；getElementIndex 全点位（0/2/3/5/8）+ 越界钳制（100→last、-1→first）。
- getParagraphElement：返回命中行的 identity、越界（100/-1）钳到末/首行。
- 分裂/合并：插入 "\n" 拆分元素；删除 "\n" 合并相邻元素且内容精确。
- filterNewlines 属性：置 true 后经 insertString 的 "\n" 变空格；tabSize/lineLimit 属性 putProperty 可改写并回读。
- StringContent 支路：new PlainDocument(new StringContent()) 同套行结构断言成立。
