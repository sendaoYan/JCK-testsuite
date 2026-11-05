# jdk.jshell.ImportSnippet 设计案

## 1. API 功能
导入片段（extends Snippet）：name/fullname/isStatic。

## 2. Java 用户如何使用
js.eval("import java.util.List;")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/ImportSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/name/fullname/isStatic。

## 5. 契约深化（2026-11-05）
- 四导入形态 name/fullname/isStatic/subKind 全表。
- 生效闭环：类型导入使 ArrayList 可用；静态导入使 max(2,3) 可用。
- 重复导入覆写（活跃导入唯一）；不可解析导入 REJECTED。
