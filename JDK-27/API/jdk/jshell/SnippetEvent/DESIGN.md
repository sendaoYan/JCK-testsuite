# jdk.jshell.SnippetEvent 设计案

## 1. API 功能
片段求值事件：snippet/status/previousStatus/value/exception/causeSnippet。

## 2. Java 用户如何使用
js.eval(...) 返回 List<SnippetEvent>。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/SnippetEvent.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 snippet/status/previousStatus/exception。
