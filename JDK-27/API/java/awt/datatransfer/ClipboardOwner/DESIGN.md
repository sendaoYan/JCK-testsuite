# java.awt.datatransfer.ClipboardOwner 测试设计

## 1. API 功能
剪贴板所有者接口。

## 2. Java 用户如何使用
匿名实现 lostOwnership。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/ClipboardOwner.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: lostOwnership 可调用。

自动化实现位于同目录 `ClipboardOwnerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 回调身份：lostOwnership 收到同一 Clipboard/Transferable 实例；(null,null) 合法。
- 接口恰 1 个抽象方法（SAM，lambda 可用）；不继承 java.util.EventListener（独立接口）——白盒锁定。
