# javax.swing.TransferHandler 测试设计

## 1. API 功能
数据传输：COPY/MOVE/NONE/COPY_OR_MOVE、createTransferable/exportToClipboard/exportAsDrag、
canImport/importData 默认实现、内置 cut/copy/paste Action。

## 2. Java 用户如何使用
`label.setTransferHandler(new TransferHandler("text"))`；子类覆写 createTransferable 定制。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/TransferHandler.java`；
JComponent.setTransferHandler()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 常量 0/1/2/3；getCopy/Cut/PasteAction 共享单例且为 Action。
- **new TransferHandler(null) 不抛**（构造器不校验）。
- 属性处理器：getSourceActions==COPY、canImport(数组) 默认 false、importData(comp,null) NPE。
- 默认实现子类验证：canImport/importData(TransferSupport) false、getVisualRepresentation(null) null、
  exportAsDrag headless no-throw。
- exportToClipboard：动作不在 sourceActions 时静默不导出（剪贴板空）；合法导出触发
  createTransferable、剪贴板持有内容且暴露 stringFlavor。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

