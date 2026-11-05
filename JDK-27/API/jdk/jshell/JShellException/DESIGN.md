# jdk.jshell.JShellException 设计案

## 1. API 功能
jshell 受检异常基类（extends Exception）；仅两个公开子类 EvalException / UnresolvedReferenceException，无公开构造器（实例只出自引擎）。

## 2. Java 用户如何使用
不直接捕获基类之外的类型：eval 事件的 exception() 通道。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 继承表三层：Exception ← JShellException ← {EvalException, URE}；无公开 ctor。
- 双通道分派：用户代码异常 → EvalException（getExceptionClassName 保真）；RECOVERABLE 声明被调用 → URE（getSnippet 指向声明）。
- 消息非空、cause 默认 null（local 引擎）、Serializable、suppressed 空。
- 易错：throwing snippet 的 status 仍是 VALID（执行失败不算声明失败）。
