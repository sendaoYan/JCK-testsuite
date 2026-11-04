# java.awt.datatransfer.Transferable 测试设计

## 1. API 功能
可传输数据接口。

## 2. Java 用户如何使用
匿名实现 getTransferDataFlavors/isDataFlavorSupported/getTransferData。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/Transferable.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: 匿名实现。

自动化实现位于同目录 `TransferableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 三方法契约（自定义实现）：flavors 顺序保留、值相等（非同一实例）识别、getTransferData 按调用计次、不支持 flavor 抛 UnsupportedFlavorException。
- 接口恰 3 个抽象方法（非 SAM）。
