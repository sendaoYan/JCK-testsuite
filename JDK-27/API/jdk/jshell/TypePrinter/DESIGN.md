# jdk.jshell.TypePrinter（内部）设计案

## 1. API 功能
类型名打印内部工具；AnonymousTypeKind 枚举区分匿名类出现位置。

## 2-3. 略

## 4. 测试设计 + 易错
AnonymousTypeKind 3 值顺序 {SUPER, DECLARE, DISPLAY} + valueOf/bogus IAE。
