# SourceCodeAnalysis.Highlight 设计案

## 1. API 功能
record(start, end, Set<Attribute>) 高亮区间。

## 2. 使用方式
highlights(String) 返回列表。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
- record 值语义：equals/hashCode/toString；3 record components。
- 易错：不做防御拷贝——传入的 EnumSet 是活引用，外部修改可见。
- 活体：`int hl1 = 1;` 两段（0..3 KEYWORD、4..7 DECLARATION）；纯语句零段。
