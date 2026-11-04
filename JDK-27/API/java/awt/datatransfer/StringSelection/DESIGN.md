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
