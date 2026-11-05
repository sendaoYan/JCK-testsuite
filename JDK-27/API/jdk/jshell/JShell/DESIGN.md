# jdk.jshell.JShell 设计案

## 1. API 功能
JShell 主入口：create/eval/drop/snippets/variables/methods/sourceCodeAnalysis。

## 2. Java 用户如何使用
try(JShell js=JShell.create()){ js.eval(...); }。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/JShell.java；test/jdk/jdk/jshell/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 create → eval VALID → snippets → drop DROPPED。

## 5. 契约深化（2026-11-05）
- 真 eval 闭环：表达式(TEMP_VAR)→var→方法跨 snippet 调用 f(21)=="42.0"→类实例化字段读取→import 生效→语句副作用。
- 错误语义：REJECTED+ErroneousSnippet+Diag(isError/getCode/compiler.err.* 前缀/位置序)；RECOVERABLE_DEFINED 与 REJECTED 分界（方法体缺符号 vs var 初始化式缺符号）。
- drop 生命周期：事件(VALID→DROPPED)、variables() 收缩、复 drop 幂等、使用被 drop 变量 REJECTED、级联。
- null/状态契约：eval/drop/varValue(null) NPE；varValue 非 VALID IAE（消息含 "must be VALID"）；外来 snippet IAE；closed 后 ISE；close 幂等。
- overwrite：2 事件（OVERWRITTEN+VALID），旧 snippet 状态 OVERWRITTEN。
- 异常通道：EvalException.getExceptionClassName 保真（NPE/NSEE/AssertionError）。
- 注入闭环：onSnippetEvent 计数/unsubscribe；tempVariableNameGenerator("$v") 跨 snippet 引用；idGenerator("X1")；onShutdown close 触发。
- Status 枚举 7 值精确顺序断言。
