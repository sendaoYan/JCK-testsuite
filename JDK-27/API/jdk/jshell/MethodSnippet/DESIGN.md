# jdk.jshell.MethodSnippet 设计案

## 1. API 功能
方法片段（extends PersistentSnippet）：name/parameterTypes/signature。

## 2. Java 用户如何使用
js.eval("int m(int a){return a*2;}")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/MethodSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/name/parameterTypes/signature。

## 5. 契约深化（2026-11-05）
- signature 语义："(int,String)int"、无参 "()void"、varargs "int..."、泛型 "T[]"。
- RECOVERABLE 方法 signature 仍可用；unresolvedDependencies 列表。
- 跨 snippet 调用与 varargs 调用闭环；methods() 只含 MethodSnippet。
