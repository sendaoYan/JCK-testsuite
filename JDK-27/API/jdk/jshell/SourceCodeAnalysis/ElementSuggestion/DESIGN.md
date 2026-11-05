# SourceCodeAnalysis.ElementSuggestion 设计案

## 1. API 功能
元素级补全候选：element()/keyword()/matchesType()/anchor()/documentation()（延迟 Supplier）。

## 2. 使用方式
经 convertor 版 completionSuggestions 获取，用于 IDE 元素级补全。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- 活体：Syst→System（CLASS kind、anchor 0、matchesType true、documentation supplier 可物化）。
- keyword 仅关键字补全时有值（成员补全 null）。
