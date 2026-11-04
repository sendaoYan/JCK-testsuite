# jdk.jfr.consumer.EventStream 设计案

## 1. API 功能
事件流抽象基类：openRepository/onEvent/onClose/setReuse/setOrdered。

## 2. Java 用户如何使用
EventStream.openRepository() 打开仓库流。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/EventStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/关键方法；黑盒 openRepository + 配置回调。
