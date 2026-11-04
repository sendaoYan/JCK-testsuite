# jdk.jshell.execution.JdiExecutionControl 设计案

## 1. API 功能
JDI 执行控制（abstract）。

## 2. Java 用户如何使用
JdiDefaultExecutionControl 的父类。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/JdiExecutionControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/public；黑盒用最小具体子类直达 protected 构造，以 JdiExecutionControl 类型直接调用 redefine 并断言 vm() 抛 EngineTerminationException；referenceType 经桥接断言 null VM 抛 NPE；vm() 为 protected abstract，只能存在性断言降级。
