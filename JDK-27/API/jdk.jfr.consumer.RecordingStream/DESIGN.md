# jdk.jfr.consumer.RecordingStream 设计案

## 1. API 功能
实时录制流（AutoCloseable）：构造/enable/setReuse/setOrdered/onEvent/onFlush。

## 2. Java 用户如何使用
new RecordingStream() 配置回调。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordingStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 构造 + 配置回调不抛异常。
