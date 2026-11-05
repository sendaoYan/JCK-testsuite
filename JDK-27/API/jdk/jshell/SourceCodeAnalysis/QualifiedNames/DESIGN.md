# SourceCodeAnalysis.QualifiedNames 设计案

## 1. API 功能
限定名补全结果：getNames()/getSimpleNameLength()/isResolvable()/isUpToDate()。

## 2. 使用方式
listQualifiedNames(String, cursor)。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- "System"→len 6/resolvable true；"Sys"→len 3/false；"java.util."→len -1（完整包前缀无残段）；空输入 len -1。
- 易错：getNames() 内容依赖异步类索引刷新，不可断言；isUpToDate() 反映时限，同因不断言。
