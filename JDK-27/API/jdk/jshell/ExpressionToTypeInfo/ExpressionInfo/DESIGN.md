# ExpressionInfo 设计案（内部）

## 1. API 功能
表达式类型分析结果：表达式树 + 五个类型名视图 + 原始/非 void 标志 + 匿名类列表。

## 2-3. 略

## 4. 测试设计 + 易错
9 字段名与 boolean 标志类型断言；需 --add-opens jdk.jshell/jdk.jshell=ALL-UNNAMED。
