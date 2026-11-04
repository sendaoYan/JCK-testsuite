# jdk.jshell.EvalException 设计案

## 1. API 功能
运行时求值异常：getExceptionClassName/getMessage（extends JShellException）。

## 2. Java 用户如何使用
eval 出错代码捕获 e.exception() 得到。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/EvalException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 1/0 触发 ArithmeticException + className。
