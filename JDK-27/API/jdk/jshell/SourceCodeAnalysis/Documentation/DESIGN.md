# SourceCodeAnalysis.Documentation 设计案

## 1. API 功能
文档候选接口：signature()/javadoc()/activeParameterIndex()（默认方法）。

## 2. 使用方式
documentation(String, cursor, expression)——cursor 必须落在方法名后（开括号处）。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- println 10 个重载、签名含参数名（"println(String x)"）、javadoc 可为 null。
- 类文档单条且 activeParameterIndex==-1；方法文档 0；未知目标空表。
- 易错：cursor 位置差 1 结果从 10 变 0。
