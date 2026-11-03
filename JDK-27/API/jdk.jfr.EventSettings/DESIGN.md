# jdk.jfr.EventSettings 设计案

## 1. API 功能
事件设置（Recording.enable 返回）：with/withoutThreshold、with/withoutStackTrace、withPeriod。

## 2. Java 用户如何使用
r.enable("jdk.FileRead").withThreshold(Duration)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventSettings.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 enable 返回 + 链式 with/without 调用。
