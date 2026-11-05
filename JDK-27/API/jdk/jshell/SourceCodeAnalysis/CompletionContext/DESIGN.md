# SourceCodeAnalysis.CompletionContext 设计案

## 1. API 功能
补全上下文标记枚举 {ANNOTATION_ATTRIBUTE, NO_PAREN, TYPES_AS_ANNOTATIONS, QUALIFIED}，经 CompletionState.completionContext() 暴露。

## 2. 使用方式
IDE 按上下文定制补全展示。

## 3. 参考来源
JDK-27 javadoc（JDK-22+ API）。

## 4. 测试设计 + 易错
4 值精确顺序 + valueOf 回环 + bogus IAE。
