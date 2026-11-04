# jdk.jfr.RecordingState 设计案

## 1. API 功能
录制状态枚举：NEW/DELAYED/RUNNING/STOPPED/CLOSED。

## 2. Java 用户如何使用
Recording.getState() 返回该枚举。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/RecordingState.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 enum/5 常量；黑盒 valueOf/values/ordinal 顺序。
