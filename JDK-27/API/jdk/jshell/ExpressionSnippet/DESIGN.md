# jdk.jshell.ExpressionSnippet 设计案

## 1. API 功能
表达式片段（extends Snippet）：name/typeName + 值。

## 2. Java 用户如何使用
js.eval("x") 表达式片段。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/ExpressionSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/name/typeName/value。

## 5. 契约深化（2026-11-05）
- 裸名→VAR_VALUE、赋值→ASSIGNMENT（name=被赋值变量、value=新值且副作用持久）。
- 新表达式→TEMP_VAR（snippet 实为 VarSnippet）；值渲染：char/String 带引号、long 无后缀、null→"null"。
- 方法调用值带引号（String）；未知符号 REJECTED。
