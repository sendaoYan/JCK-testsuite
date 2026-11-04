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

## 5. 契约深化（本轮追加）
- 空剪贴板语义：getContents(null/requestor) → null；getAvailableDataFlavors → 空数组；isDataFlavorAvailable → false；getData → UnsupportedFlavorException。
- setContents 语义：void 返回（owner 通知经 lostOwnership 异步回调，非同步断言）；null contents 清空剪贴板；null owner 合法；二次 setContents 替换；requestor 参数被忽略。
- getData 反例：不支持的 flavor → UnsupportedFlavorException。
- FlavorListener 集合语义：注册/移除/重复注册/移除未注册 no-op；非空时 getFlavorListeners 每次返回新数组。
- 版本差异（行为锁定）：JDK-8 addFlavorListener 重复注册存两份（remove 一次删一份）；JDK-9+（含 JDK-27）去重（重复 add 后长度仍为 1）。双侧断言相反。
- 白盒：setContents/getContents 为 synchronized、setContents 返回 void。
