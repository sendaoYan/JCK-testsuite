# javax.swing.text.DefaultStyledDocument — DESIGN

## 1. API 功能
`StyledDocument` 的标准实现（继承 `AbstractDocument`）。方法分组：
- 构造：`DefaultStyledDocument()`、`DefaultStyledDocument(StyleContext)`、`DefaultStyledDocument(Content, StyleContext)`。
- 结构：`getDefaultRootElement()`、`getParagraphElement(int)`、`getCharacterElement(int)`、`removeElement(Element)`。
- 样式：`addStyle/getStyle/removeStyle/getStyleNames`、`setLogicalStyle/getLogicalStyle`。
- 属性：`setCharacterAttributes`、`setParagraphAttributes`。
- 颜色/字体解析：`getForeground/getBackground/getFont`。
- 缓冲构建（protected）：`create(ElementSpec[])`、`insert(int,ElementSpec[])`。

## 2. Java 用户如何使用
`new DefaultStyledDocument()` → `insertString(0,"abc\ndef",null)` → `getParagraphElement(0)`/`getCharacterElement(0)` 定位 → `addStyle("s",null)` 创建样式 → `setCharacterAttributes(0,3,attr,false)` 上色 → `setLogicalStyle(0,style)` 指定段落逻辑样式 → `getForeground/style` 解析颜色。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:104`：`createDefaultDocument()` 返回 `new DefaultStyledDocument()`。
- `test/jdk/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0,"this",null); doc.setCharacterAttributes(...)`。
- `test/jdk/javax/swing/text/DefaultStyledDocument/DocNegLenCharAttrTest.java`：`doc = new DefaultStyledDocument(); text.setDocument(doc); doc.setCharacterAttributes(6,-5,attr,true)`（验证负长度不抛异常）。

## 4. 测试用例设计 + 自动化实现（`DefaultStyledDocumentTest.testBlackBox`）
- 根为 section、`getParagraphElement/getCharacterElement` 结构随换行更新。
- 文本 insert/getText/remove 往返。
- 样式 `addStyle/getStyle/removeStyle/getStyleNames`（含 default）。
- `setLogicalStyle/getLogicalStyle` 往返。
- `setCharacterAttributes/setParagraphAttributes` 反映到元素属性。
- `getForeground/getBackground/getFont` 经 `StyleConstants` 解析固定颜色/字体族。
- `removeElement` 删除段落文本与结构；`DefaultStyledDocument(StyleContext)` 构造可用。

## 5. 契约深化（本轮追加）
`testTypeShape/testElementTree/testCharacterAttributes/testParagraphAttributesAndLogicalStyle/testStyleRegistry/testColorsAndFont/testChangeEventsAndUndo`（89 断言）：
- 类型面与常量：BUFFER_SIZE_DEFAULT=4096；方法面 setCharacterAttributes/setParagraphAttributes/setLogicalStyle/getLogicalStyle/getCharacterElement/getParagraphElement/getForeground/getBackground/getFont/addStyle/removeStyle/getStyle/getStyleNames。
- 元素树：section→paragraph→content 三级；空文档各层区间 [0,1)；插入后 section end 随内容、每段一个 content run；getParagraphElement identity 与钳制；getCharacterElement 返回 run（parent 是段）并钳到末 run。
- 字符属性：styled 范围内 isBold、区间外不变；非 replace 合并保留旧值、replace=true 清除其它；插入新文本不继承 run 属性；零长度 apply 是 no-op；越界偏移钳到末 run 并应用（头部 run 不受影响）。
- 段落属性：段落元素带 resolver 条目（NameAttribute="resolver"）指向逻辑 style；ALIGN_CENTER==1/ALIGN_RIGHT==2 交叉验证；setLogicalStyle 经 resolver 生效。
- style 注册表：addStyle/getStyle/removeStyle、匿名 style 名 null；逻辑 style 缺省链。
- 颜色/字体：getForeground/getBackground 默认黑、属性覆盖生效；getFont 由 attrs 派生（Monospaced 12 默认）。
- 事件/撤销：changedUpdate 触发 "C0:3:CHANGE"（offset/length/type=CHANGE）；样式修改产生 undoable edit（presentationName "style change"、significant）且 undo/redo 回滚/重放属性。
