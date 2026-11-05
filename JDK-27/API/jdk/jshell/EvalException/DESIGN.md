# jdk.jshell.EvalException 设计案

## 1. API 功能
运行时求值异常：getExceptionClassName/getMessage（extends JShellException）。

## 2. Java 用户如何使用
eval 出错代码捕获 e.exception() 得到。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/EvalException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 1/0 触发 ArithmeticException + className。

## 5. 契约深化（2026-11-05）
- 类名保真矩阵：checked Exception/NPE/AIOOBE/StackOverflowError/snippet 自定义异常类。
- 消息透传、getStackTrace 可用、cause null（local 引擎）、Serializable。
- throwing snippet status 仍 VALID 可重跑。
