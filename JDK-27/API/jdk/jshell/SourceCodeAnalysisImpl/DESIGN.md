# jdk.jshell.SourceCodeAnalysisImpl（内部）设计案

## 1. API 功能
SourceCodeAnalysis 实现；ClassIndex 是其类索引（timestamp/forPath/packages/classSimpleName2FQN）。

## 2. 使用方式
不公开；仅结构白盒（--add-opens 反射）。

## 3. 参考来源
JDK-27 源码结构。

## 4. 测试设计 + 易错
- ClassIndexTest：具体类、4 public 字段（timestamp long、forPath Path、packages Set、classSimpleName2FQN Map<String,Collection<String>>）、单 public ctor (long,Path,Set,Map)。
