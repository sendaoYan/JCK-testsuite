# jdk.jfr.consumer.RecordedClassLoader 设计案

## 1. API 功能
记录的类加载器（final extends RecordedObject）：getName/getId/getType。

## 2. Java 用户如何使用
从 RecordedClass.getClassLoader() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClassLoader.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/extends RecordedObject/关键方法；黑盒 获取类加载器名称（可空则跳过）。
