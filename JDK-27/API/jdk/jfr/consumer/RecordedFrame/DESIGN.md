# jdk.jfr.consumer.RecordedFrame 设计案

## 1. API 功能
记录的栈帧：getType/getMethod/isJavaFrame/getLineNumber。

## 2. Java 用户如何使用
RecordedStackTrace.getFrames().get(0)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedFrame.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 帧类型/方法/行号。
