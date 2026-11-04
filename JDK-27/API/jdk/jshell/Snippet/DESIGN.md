# jdk.jshell.Snippet 设计案

## 1. API 功能
片段基类（abstract）：id/kind/subKind/source/toString。

## 2. Java 用户如何使用
所有片段（var/method/type/import/erroneous）的基类。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/Snippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 id/kind/subKind/source。
