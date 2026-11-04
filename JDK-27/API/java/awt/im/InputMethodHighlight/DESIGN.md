# java.awt.im.InputMethodHighlight 测试设计

## 1. API 功能
输入法高亮描述（是否选中、状态、变体、样式）。

## 2. Java 用户如何使用
new InputMethodHighlight(selected, state[, variation[, style]]); isSelected/getState/getVariation/getStyle；常量。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/im/InputMethodHighlight.java

## 4. 测试用例设计 + 自动化实现
白盒: 声明 isSelected/getState；黑盒: 常量、各构造、getter。

自动化实现位于同目录 `InputMethodHighlightTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：两常量；4 预定义高亮；构造器变体；无 equals 恒等。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
