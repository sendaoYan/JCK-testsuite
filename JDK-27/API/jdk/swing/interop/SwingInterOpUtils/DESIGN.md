# jdk.swing.interop.SwingInterOpUtils 设计案

## 1. API 功能
Swing 互操作工具：postEvent/grab/ungrab/isUngrabEvent。

## 2. Java 用户如何使用
SwingInterOpUtils.isUngrabEvent(ev)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/SwingInterOpUtils.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+isUngrabEvent(普通 AWTEvent)。

## 5. 契约深化（JDK-27）

- GRAB_EVENT_MASK 为非零高位常量（与 AWT 事件位不重叠）。
- postEvent(EventQueue, AWTEvent) 在 headless 下对全新 EventQueue 安全投递不抛；peekEvent 不保证可见（异步语义，不作正向断言）。
- isUngrabEvent：普通自定义 AWTEvent 与 ActionEvent 均为 false。
- 工具类形态：public 具体、全部 public 方法 static、public 构造器保留。
