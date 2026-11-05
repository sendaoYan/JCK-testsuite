# jdk.jshell.SourceCodeAnalysis 设计案

## 1. API 功能
源码分析（abstract）：analyzeCompletion/completionSuggestions/highlights/documentation。

## 2. Java 用户如何使用
js.sourceCodeAnalysis()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/SourceCodeAnalysis.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 补全完整性 + 建议 System + highlights。

## 5. 契约深化（2026-11-05）
- analyzeCompletion 切分循环矩阵（source/remaining 精确切分、COMPLETE_WITH_SEMI 自动补分号、未完成 source null）。
- completionSuggestions：anchor 出参位置、静态成员 vs 表达式接收者（注：表达式接收者补全受索引刷新时序影响，实测跨进程不稳，明确不设断言并记录）。
- documentation：cursor 必须在方法名后开括号处；10 个 println 重载、签名含参数名、javadoc 可 null、类文档 activeParameterIndex==-1。
- analyzeType：String/int/Object/boolean/空输入 null。
- listQualifiedNames：len/isResolvable 语义；内容与 isUpToDate 因异步索引不断言。
- wrapper(s)：REPL.$JShell$ 命名、package REPL; 前缀、位置映射往返、双语句单 wrapper 只包首单元。
- sourceToSnippets：*UNASSOCIATED* id、ERRONEOUS 兜底；dependents 非空性。
- highlights：KEYWORD/DECLARATION 区间序、纯语句零区间。
