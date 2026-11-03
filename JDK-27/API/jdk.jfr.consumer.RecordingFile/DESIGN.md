# jdk.jfr.consumer.RecordingFile 设计案

## 1. API 功能
读取 .jfr 文件：构造/hasMoreEvents/readEvent/readAllEvents。

## 2. Java 用户如何使用
new RecordingFile(path) 逐事件读取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordingFile.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 dump → readEvent 计数 + readAllEvents 非空。
