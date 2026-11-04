# jdk.jfr.consumer.RecordedMethod 设计案

## 1. API 功能
记录的方法：getName/getDescriptor/getType/isHidden。

## 2. Java 用户如何使用
RecordedFrame.getMethod()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedMethod.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 方法名/描述符/声明类。
