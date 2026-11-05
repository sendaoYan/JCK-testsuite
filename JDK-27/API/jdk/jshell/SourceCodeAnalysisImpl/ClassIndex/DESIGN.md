# ClassIndex 设计案（内部）

## 1. API 功能
SourceCodeAnalysisImpl 的类索引：路径→时间戳/包集/简单名→FQN 集合。

## 2-3. 略（内部类，结构白盒）

## 4. 测试设计 + 易错
4 字段类型断言（timestamp 是 long 不是集合——易错）；ctor 签名 (long, Path, Set, Map)。
