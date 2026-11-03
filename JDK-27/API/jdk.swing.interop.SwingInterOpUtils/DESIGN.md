# jdk.swing.interop.SwingInterOpUtils 设计案

## 1. API 功能
Swing 互操作工具：postEvent/grab/ungrab/isUngrabEvent。

## 2. Java 用户如何使用
SwingInterOpUtils.isUngrabEvent(ev)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/SwingInterOpUtils.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+isUngrabEvent(普通 AWTEvent)。
