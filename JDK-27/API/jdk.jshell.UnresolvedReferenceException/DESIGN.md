# jdk.jshell.UnresolvedReferenceException 设计案

## 1. API 功能
未解析引用异常：getSnippet。

## 2. Java 用户如何使用
方法引用未定义变量后调用触发。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/UnresolvedReferenceException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 触发 + getSnippet 一致。
