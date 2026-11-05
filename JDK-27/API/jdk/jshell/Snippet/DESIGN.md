# jdk.jshell.Snippet 设计案

## 1. API 功能
片段基类（abstract）：id/kind/subKind/source/toString。

## 2. Java 用户如何使用
所有片段（var/method/type/import/erroneous）的基类。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/Snippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 id/kind/subKind/source。

## 5. 契约深化（2026-11-05）
- 类层级白盒：PersistentSnippet/DeclarationSnippet/Var/Method/TypeDecl/Import/Statement/Expression/Erroneous 的 extends 关系逐一断言。
- id 稳定且互异；source() 逐字符 round-trip（含空格保持）；drop 后 id/kind/source 仍可查。
- subKind().kind() == kind() 对 15 种源码形态全成立；六种 Kind+ERRONEOUS 全部经 eval 可达。
