# jdk.jfr.consumer.RecordedClass 设计案

## 1. API 功能
记录的事件类（final extends RecordedObject）：getName/getId/getModifiers/getClassLoader。

## 2. Java 用户如何使用
从 RecordedEvent 栈帧方法 getType() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClass.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/关键方法；黑盒 录制自定义事件 → 栈帧 → 类名/id/modifiers。
