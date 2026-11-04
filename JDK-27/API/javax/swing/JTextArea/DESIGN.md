# javax.swing.JTextArea 黑盒行为测试设计

## 1. API 功能
JTextArea 是多行文本区域，继承 JTextComponent。

- 构造器：`JTextArea()`、`JTextArea(String)`、`JTextArea(int rows, int columns)`、`JTextArea(String, int, int)`、`JTextArea(Document)`、`JTextArea(Document, String, int, int)`。
- 文本编辑：`append(String)`、`insert(String, int)`、`replaceRange(String, int, int)`。
- 行列：`getRows/setRows`、`getColumns/setColumns`。
- 换行：`getLineWrap/setLineWrap`、`getWrapStyleWord/setWrapStyleWord`。
- 制表符：`getTabSize/setTabSize`。
- 行/偏移查询：`getLineCount()`、`getLineOfOffset(int)`、`getLineStartOffset(int)`、`getLineEndOffset(int)`（末行 endOffset 隐藏隐式换行）。
- UI：`getUIClassID()` 返回 "TextAreaUI"。

## 2. Java 用户如何使用
- `JTextArea ta = new JTextArea("hello", 10, 40);` —— 初始化文本与行列。
- `ta.append(line + "\n");` 逐行追加日志。
- `ta.insert("XY", 1);` 指定位置插入。
- `ta.replaceRange("ZZ", 1, 3);` 区间替换。
- `ta.setLineWrap(true); ta.setWrapStyleWord(true);` 自动换行。
- 行号定位：`getLineOfOffset`/`getLineStartOffset`/`getLineEndOffset`。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/FullScreen/AllFramesMaximize/AllFramesMaximize.java` 用 JTextArea 展示文本。
- `test/jdk/java/awt/Robot/HiDPIScreenCapture/*` 用 JTextArea 渲染固定文本做像素断言。
- `test/jdk/java/awt/TrayIcon/DisposeInActionEventTest.java` 用 JTextArea 作为日志输出面板。
真实项目（日志查看器、代码编辑器简化版）通用模式：`append` 追加 + `setLineWrap` + `replaceRange` 编辑。以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JTextAreaTest.testBlackBox()`）：
1. 默认值：空文本 / rows 0 / columns 0 / PlainDocument / getUIClassID。
2. 各构造器文本与行列语义（含 Document 引用保留）。
3. append/insert/replaceRange 组合链路（追加→插入→替换→删除）。
4. lineWrap/wrapStyleWord/tabSize 往返。
5. 多行文本的行/偏移查询（getLineCount、getLineOfOffset、getLineStartOffset、getLineEndOffset）。
6. setCaretPosition/getCaretPosition 往返。
