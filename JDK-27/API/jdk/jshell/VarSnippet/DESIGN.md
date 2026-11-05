# jdk.jshell.VarSnippet 设计案

## 1. API 功能
变量片段（extends PersistentSnippet）：name/typeName。

## 2. Java 用户如何使用
js.eval("int x = 1;")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/VarSnippet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 kind/name/typeName。

## 5. 契约深化（2026-11-05）
- 默认值通道：无初始化式 int→"0"、String→"null"。
- typeName/value 渲染矩阵：int/double/boolean/char('x' 带引号)/long(渲染无后缀)/String(带引号)/int[]("int[2] { 1, 2 }" jshell 专用格式)/Object null。
- 覆写：同名重declare 与类型变更均 2 事件且生效。
- jshell 放宽：final var 可再赋值（REPL 语义，varValue 更新）——记录引擎实际行为。
