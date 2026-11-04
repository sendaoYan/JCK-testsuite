# jdk.jfr.EventFactory 设计案

## 1. API 功能
动态创建事件类型并提交：create/getEventType/newEvent。

## 2. Java 用户如何使用
EventFactory.create(annotations, fields).newEvent().commit()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 create → 类型名 → newEvent → set → commit。
