# java.awt.datatransfer.Clipboard 测试设计

## 1. API 功能
本地剪贴板（Clipboard(String) 构造的本地实例），保存 Transferable 内容，支持 FlavorListener。

## 2. Java 用户如何使用
new Clipboard(name)；setContents(Transferable, owner)；getContents/getData/getAvailableDataFlavors/isDataFlavorAvailable；add/remove/getFlavorListeners。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/Clipboard.java

## 4. 测试用例设计 + 自动化实现
白盒: 声明 setContents/getData/addFlavorListener；黑盒: 本地剪贴板 set/get 内容、getData、flavor 判断、listener 增删。注意: 本地 Clipboard(String) 无需系统剪贴板，headless 可用。

自动化实现位于同目录 `ClipboardTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
