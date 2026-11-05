# SourceCodeAnalysis.Suggestion 设计案

## 1. API 功能
补全候选接口：continuation()/matchesType()。

## 2. 使用方式
completionSuggestions(String,int,int[]) 返回。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- anchor 出参：顶层标识符起点 0、点号后 = 点号后位置。
- "String." 只补静态成员（valueOf(/class/CASE_INSENSITIVE_ORDER——实例方法不在类名接收者上）；前缀过滤 valueO→只 valueOf。
