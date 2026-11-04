# jdk.jfr.Event 设计案

## 1. API 功能
所有 JFR 事件基类（abstract），子类用注解描述元数据，begin/end/commit 提交。

## 2. Java 用户如何使用
继承 Event，加 @Name/@Label/@Description 与字段，new → begin → 赋值 → end → commit。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Event.java；test/jdk/jdk/jfr/ 事件类。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/关键方法/继承；黑盒 begin/end/commit + 注解回读。
