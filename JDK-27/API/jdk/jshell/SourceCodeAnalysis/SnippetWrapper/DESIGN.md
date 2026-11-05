# SourceCodeAnalysis.SnippetWrapper 设计案

## 1. API 功能
包装视图接口：source()/wrapped()/fullClassName()/kind()/sourceToWrappedPosition()/wrappedToSourcePosition()。

## 2. 使用方式
wrapper(Snippet)/wrappers(String) 查看源码→REPL 宿主类包装。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- fullClassName "REPL.$JShell$N"；wrapped 以 "package REPL;" 开头含 "class $JShell$N"。
- 位置映射 0↔51 往返；逐 3 步采样正向映射非负。
- 易错：两语句输入只产出 1 个 wrapper 且只包第一个单元。
