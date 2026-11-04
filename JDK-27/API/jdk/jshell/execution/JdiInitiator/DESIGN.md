# jdk.jshell.execution.JdiInitiator 设计案

## 1. API 功能
JDI 启动器（具体类）：vm()/process()。

## 2. Java 用户如何使用
由 JDI 执行控制内部使用。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/JdiInitiator.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 具体类/public/vm/process；黑盒降级。
