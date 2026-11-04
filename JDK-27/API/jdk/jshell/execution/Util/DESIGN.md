# jdk.jshell.execution.Util 设计案

## 1. API 功能
执行工具类：forwardExecutionControl 等静态方法。

## 2. Java 用户如何使用
静态工具，转发执行控制。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/Util.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 具体类/方法存在；黑盒降级。
