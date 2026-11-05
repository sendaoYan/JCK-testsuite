# jdk.jshell.ExpressionToTypeInfo（内部）设计案

## 1. API 功能
表达式类型分析内部工具；ExpressionInfo 是其结果记录。

## 2-3. 略（包私有，结构白盒 + add-opens 反射）

## 4. 测试设计 + 易错
ExpressionInfo 9 字段精确集合（tree/isPrimitiveType/typeName/accessibleTypeName/declareTypeName/fullTypeName/displayTypeName/isNonVoid/anonymousClasses）。
