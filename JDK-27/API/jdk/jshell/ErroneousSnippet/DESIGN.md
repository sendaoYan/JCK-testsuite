# jdk.jshell.ErroneousSnippet 设计案

## 1. API 功能
错误片段（extends Snippet）：kind=ERRONEOUS、probableKind、source。

## 2. Java 用户如何使用
js.eval("int = ;") 产生 ErroneousSnippet。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/ErroneousSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/probableKind/source。

## 5. 契约深化（2026-11-05）
- probableKind 矩阵：坏 var→VAR、坏 class→TYPE_DECL、坏方法→METHOD、坏 import→IMPORT、坏语句→STATEMENT、垃圾("###")→EXPRESSION。
- 全部 REJECTED+有 diag+id/source 可查；不进任何声明流（variables()/methods() 无注册）。
