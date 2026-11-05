# Wrap.Range 设计案（内部）

## 1. API 功能
final 值类：begin/end int 字段 + part/length/isEmpty/verify 工具。

## 2-3. 略

## 4. 测试设计 + 易错
无 equals/hashCode 覆写（恒等）；toString "Range[1,5)"；length=end-begin。反射需 add-opens + setAccessible。
