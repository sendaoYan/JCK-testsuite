# SourceCodeAnalysis.Completeness 设计案

## 1. API 功能
输入完整性枚举 {COMPLETE, COMPLETE_WITH_SEMI, DEFINITELY_INCOMPLETE, CONSIDERED_INCOMPLETE, EMPTY, UNKNOWN} + isComplete()。

## 2. 使用方式
REPL 逐行判断是否可执行（analyzeCompletion().completeness()）。

## 3. 参考来源
JDK-27 javadoc + 实测 parser。

## 4. 测试设计 + 易错
- 6 值精确顺序；isComplete 表（EMPTY/DEFINITE/CONSIDERED 为 false，UNKNOWN 为 true——易错）。
- 语句矩阵：表达式/带分号声明/缺分号声明(COMPLETE_WITH_SEMI 且 source 自动补分号)/开块/裸类/方法/空白/注释/开注释/throw 开调用。
