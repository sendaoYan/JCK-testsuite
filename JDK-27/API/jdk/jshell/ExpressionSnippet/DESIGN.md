# jdk.jshell.ExpressionSnippet 设计案

## 1. API 功能
表达式片段（extends Snippet）：name/typeName + 值。

## 2. Java 用户如何使用
js.eval("x") 表达式片段。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/ExpressionSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/name/typeName/value。
