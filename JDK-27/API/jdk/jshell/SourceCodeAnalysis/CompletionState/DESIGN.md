# SourceCodeAnalysis.CompletionState 设计案

## 1. API 功能
补全态五元接口：availableUsingSimpleName/completionContext/selectorType/elementUtils/typeUtils。

## 2. 使用方式
ElementSuggestionConvertor.convert 的第一实参。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- 结构：5 方法精确集合。
- 活体：convertor 回调内 elementUtils/typeUtils 非空、getTypeElement("java.lang.String") 命中、bogus null。
- 易错：JavacTask 在回调返回后关闭，回调外再查 element 会 IllegalStateException——断言必须放在回调内。
