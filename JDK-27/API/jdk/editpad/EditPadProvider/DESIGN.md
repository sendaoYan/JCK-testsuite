# jdk.editpad.EditPadProvider 契约设计

## 1. API 功能
`EditPadProvider` 实现 `jdk.internal.editor.spi.BuildInEditorProvider`，是 jdk.editpad 模块（jshell 的编辑后端）里的 Swing 文本编辑垫片：`rank()` 返回 5（高于内置兜底），`edit(String windowLabel, String initialText, Consumer<String> saveHandler, Consumer<String> errorHandler)` 弹出一个可保存的编辑窗，保存回调把全文交给 saveHandler，出错走 errorHandler。public 无参构造器供服务装载。

## 2. Java 用户如何使用
普通用户不直接调用：jshell /edit 触发 ServiceLoader 装载 BuildInEditorProvider，按 rank 择优，落到这里即得到一个 Swing 编辑窗。headless 环境下创建窗口会抛 HeadlessException，因此消费方是交互式终端会话。

## 3. 开源代码实际用法调研
- OpenJDK jdk.editpad 仅此一个提供者；jshell 的 ProviderUtil 走 ServiceLoader + rank 排序。
- 社区方案（如 jedit pad、repl 编辑器插件）以同签名 Consumer 回调模式实现外部编辑器，管道/临时文件形态。

## 4. 测试用例设计+自动化实现
- 结构契约：public 无参构造器可 newInstance；rank() == 5；edit 精确签名 (String, String, Consumer<String>, Consumer<String>) 且返回 void、public。
- SPI 装配：Class.forName("jdk.internal.editor.spi.BuildInEditorProvider") 可赋值、其 rank() 返回 int（对 jdk.jshell 内部 SPI 的稳定锚定）。
- 不实际弹窗（headless 安全），只锁方法面。
- 自动化：jtreg `run main` + `check()`，@modules jdk.editpad/jdk.editpad:+open jdk.jshell/jdk.internal.editor.spi:+open。
