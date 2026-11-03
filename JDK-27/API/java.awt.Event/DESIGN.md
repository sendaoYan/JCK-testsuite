# java.awt.Event 测试设计

## 1. API 功能
遗留事件数据类（已弃用），携带 target/when/id/x/y/key/modifiers/arg 等公共字段。

## 2. Java 用户如何使用
new Event(target,id,arg) 或 new Event(target,when,id,x,y,key,modifiers,arg)；translate 平移坐标；shiftDown/controlDown/metaDown 判断修饰键。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Event.java

## 4. 测试用例设计 + 自动化实现
白盒: implements Serializable、声明 translate/shiftDown/...；黑盒: 字段赋值、translate、修饰键判定、toString。

自动化实现位于同目录 `EventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
