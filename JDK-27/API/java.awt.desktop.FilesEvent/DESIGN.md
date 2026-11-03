# java.awt.desktop.FilesEvent 测试设计

## 1. API 功能
文件列表桌面事件基类，继承 AppEvent，提供 getFiles()。

## 2. Java 用户如何使用
构造器包私有，由 OpenFilesEvent/PrintFilesEvent 子类实例化。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/desktop/FilesEvent.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 AppEvent、声明 getFiles；黑盒降级: 包私有构造且子类需图形环境。

自动化实现位于同目录 `FilesEventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
