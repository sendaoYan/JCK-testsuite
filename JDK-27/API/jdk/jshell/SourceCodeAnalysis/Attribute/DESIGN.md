# SourceCodeAnalysis.Attribute 设计案

## 1. API 功能
高亮属性枚举 {DECLARATION, DEPRECATED, KEYWORD}。

## 2. 使用方式
Highlight.attributes() 集合。

## 3. 参考来源
JDK-27 javadoc + 实测。

## 4. 测试设计 + 易错
3 值精确顺序；活体：int 关键字段 KEYWORD、变量名段 DECLARATION（EnumSet 单值）。
