# jdk.jshell.execution.JdiDefaultExecutionControl 设计案

## 1. API 功能
默认 JDI 执行控制（extends JdiExecutionControl，具体类）。

## 2. Java 用户如何使用
由 JShell 远程执行创建。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/JdiDefaultExecutionControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 具体类/public/继承；黑盒降级。
