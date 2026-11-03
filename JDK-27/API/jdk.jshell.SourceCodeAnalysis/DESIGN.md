# jdk.jshell.SourceCodeAnalysis 设计案

## 1. API 功能
源码分析（abstract）：analyzeCompletion/completionSuggestions/highlights/documentation。

## 2. Java 用户如何使用
js.sourceCodeAnalysis()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/SourceCodeAnalysis.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 补全完整性 + 建议 System + highlights。
