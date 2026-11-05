# SourceCodeAnalysis.CompletionInfo 设计案

## 1. API 功能
analyzeCompletion 结果三元组：completeness()/source()/remaining()。

## 2. 使用方式
驱动 REPL 输入切分循环。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- 切分语义：source=首个完整单元（不含尾随空白，空白归 remaining）；remaining 保留余下输入。
- 未完成时 source()==null（不是空串——易错）；COMPLETE_WITH_SEMI 时 source 自动追加 ";"。
- 循环驱动三单元输入到耗尽。
