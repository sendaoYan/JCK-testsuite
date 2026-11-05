# jdk.jshell.PersistentSnippet 设计案

## 1. API 功能
可持久化片段基类（abstract）：name。

## 2. Java 用户如何使用
VarSnippet/MethodSnippet/TypeDeclSnippet 的父类。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/PersistentSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒 VarSnippet instanceof PersistentSnippet。

## 5. 契约深化（2026-11-05）
- 持久性矩阵：var/method/typedecl/import 是 Persistent；表达式/纯语句(if)/坏 snippet 不是。
- 注意 `pv1++` 有值 → 临时变量（VAR，持久）不是语句。
- name 跨 drop 稳定；Kind.isPersistent 与类型归属一致。
