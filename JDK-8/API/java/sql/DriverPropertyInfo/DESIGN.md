# java.sql.DriverPropertyInfo 测试设计案（JDK-8）

> 完整契约：构造器字段初始化、公开字段默认值、字段可变、null 参数。

## 1. API 提供功能
- 构造器（1）：`(String name, String value)`。
- 公开字段（5）：`name`、`value`、`description`（null）、`required`（false）、`choices`（null）。

## 2. 使用方式
驱动在 `Driver.getPropertyInfo()` 中返回属性数组；用户设置 name/value/required/choices/description。

## 3. 参考来源
- javadoc `java/sql/DriverPropertyInfo.html`；JCK `api/java_sql/DriverPropertyInfo`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 构造器 name/value 初始化 + 默认值 | testConstructor |
| required 默认 false | testDefaults |
| 5 字段全部可变 | testMutability |
| null name/value 接受 | testNullArgs |
