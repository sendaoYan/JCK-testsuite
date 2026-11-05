# jdk.jshell.DeclarationSnippet 设计案

## 1. API 功能
声明基类（var/method/typedecl 的公共抽象父）；配合 JShell.unresolvedDependencies(DeclarationSnippet)。

## 2. Java 用户如何使用
RECOVERABLE 声明经 unresolvedDependencies 查缺失符号表，格式 "method name()"。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 归属矩阵：var/method/typedecl 是 DeclarationSnippet；import 是 Persistent 但不是 Declaration（注意 instanceof 两侧无继承关系需先上转型为 Snippet 再判，否则编译错）。
- unresolvedDependencies：单依赖/双依赖列表、解决一个后收缩、已解决声明为空、null 参数 NPE。
- 易错：RECOVERABLE_NOT_DEFINED 的类型声明（缺超类型）与 unresolvedDependencies 通道不同（后者只报方法体符号）。
