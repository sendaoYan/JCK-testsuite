# jdk.jshell.SnippetEvent 设计案

## 1. API 功能
片段求值事件：snippet/status/previousStatus/value/exception/causeSnippet。

## 2. Java 用户如何使用
js.eval(...) 返回 List<SnippetEvent>。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/SnippetEvent.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 snippet/status/previousStatus/exception。

## 5. 契约深化（2026-11-05）
- 事件文法全字段矩阵：fresh(NONEXISTENT→VALID)/expression(值通道)/drop(VALID→DROPPED)/overwrite(2 事件，旧 OVERWRITTEN 新 VALID)/exception(EvalException+value null+status VALID)。
- 级联事件：补定义依赖时依赖方升级事件包含在 res 列表中。
- var 声明事件携带初始化值（"1"）；isSignatureChange 语义（声明/临时变量 true）。
- event.snippet() 与 snippets() 流中对象同一（==）。
