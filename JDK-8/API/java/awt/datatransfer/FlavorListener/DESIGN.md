# java.awt.datatransfer.FlavorListener 测试设计

## 1. API 功能
flavor 变化监听接口，继承 EventListener。

## 2. Java 用户如何使用
匿名实现 flavorsChanged。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/FlavorListener.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口、继承 EventListener；黑盒: flavorsChanged 可调用。

自动化实现位于同目录 `FlavorListenerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 方法签名：flavorsChanged(FlavorEvent) public abstract void；恰 1 个抽象方法；事件实例精确传递。
