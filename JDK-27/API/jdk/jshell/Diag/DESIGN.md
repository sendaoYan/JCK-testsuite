# jdk.jshell.Diag 设计案

## 1. API 功能
JShell 诊断信息（错误/警告）：isError/getCode/getPosition/getLineNumber。

## 2. Java 用户如何使用
由 JShell.diagnostics(ErroneousSnippet) 获取，读取诊断级别/代码/位置。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/Diag.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 eval 错误片段 → diagnostics 非空 + isError。

## 5. 契约深化（2026-11-05）
- 位置契约：start>=0、end>=start、start<=pos<=end；getCode "compiler.err.*"；getMessage(Locale.ROOT) 非空。
- 多 diag 有序非空；合法/RECOVERABLE 声明 diag 为空（与编译错误通道分界）。
- NOPOS==-1L；diag 查询幂等；drop 后仍可查。
