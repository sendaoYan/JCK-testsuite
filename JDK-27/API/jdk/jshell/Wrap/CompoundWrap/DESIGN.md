# Wrap.CompoundWrap 设计案（内部）

## 1. API 功能
多段 Wrap 组合；继承 Wrap 实现 GeneralWrap（wrapped + snippet/wrap 索引与行号映射族）。

## 2-3. 略

## 4. 测试设计 + 易错
GeneralWrap 方法面（snippetIndexToWrapIndex 等 6+）；Wrap.methodWrap("return 42;") 闭环 wrapped() 含源体。
