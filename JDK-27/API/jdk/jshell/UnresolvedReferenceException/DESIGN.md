# jdk.jshell.UnresolvedReferenceException 设计案

## 1. API 功能
未解析引用异常：getSnippet。

## 2. Java 用户如何使用
方法引用未定义变量后调用触发。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/UnresolvedReferenceException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 触发 + getSnippet 一致。

## 5. 契约深化（2026-11-05）
- 双触发：缺变量与缺方法均 URE；getSnippet()==声明本体；消息含方法名。
- 解决依赖后同调用成功且无 URE（前后对照）。
- 反例：var 初始化式缺符号→REJECTED（无 URE）；实例化 RECOVERABLE_NOT_DEFINED 类型→REJECTED（无 URE）。
