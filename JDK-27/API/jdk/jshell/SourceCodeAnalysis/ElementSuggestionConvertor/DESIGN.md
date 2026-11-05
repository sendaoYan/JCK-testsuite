# SourceCodeAnalysis.ElementSuggestionConvertor 设计案

## 1. API 功能
函数式接口 convert(CompletionState, List<ElementSuggestion>) -> List<S>：把引擎元素补全映射为任意类型。

## 2. 使用方式
completionSuggestions(String,int,convertor) 泛型通道。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- 恒等/映射到 String/返回空表三态：convertor 完全掌控输出。
- 回调内 state 与 list 非空断言。
