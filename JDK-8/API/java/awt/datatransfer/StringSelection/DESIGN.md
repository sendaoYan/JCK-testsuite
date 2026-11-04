# java.awt.datatransfer.StringSelection 测试设计

## 1. API 功能
字符串可传输对象，实现 Transferable+ClipboardOwner。

## 2. Java 用户如何使用
new StringSelection(String)；getTransferDataFlavors/isDataFlavorSupported/getTransferData。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/StringSelection.java

## 4. 测试用例设计 + 自动化实现
白盒: implements Transferable+ClipboardOwner；黑盒: flavors、getTransferData string/plainText、UnsupportedFlavorException。

自动化实现位于同目录 `StringSelectionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- flavors 数组契约：恰 2 个（stringFlavor 在前、plainTextFlavor 在后）、防御性拷贝（改返回数组不影响内部）、每次调用返回新数组。
- plainTextFlavor 数据为 Reader：读全文等于原串；每次 getTransferData 返回从头开始的 新 Reader。
- isDataFlavorSupported(null) → NPE（实现直接调 flavor.equals，锁定参考行为）。
- 边界串：空串（stringFlavor 返回 ""、Reader EOF）、Unicode 串、null 构造参数合法（getTransferData 返回 null、Reader EOF）。
- UnsupportedFlavorException 的 message 为该 flavor 的 humanPresentableName。
