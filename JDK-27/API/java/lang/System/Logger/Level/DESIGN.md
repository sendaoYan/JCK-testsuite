# System.Logger.Level — 设计方案

## 1. API 功能
`System.Logger.Level`（enum）是日志级别：`ALL < TRACE < DEBUG < INFO < WARNING < ERROR < OFF`（共 7 个），提供 `getName()`、`getSeverity()`（数值，越大越严重）。

## 2. Java 用户如何使用
- 作为 `System.Logger.isLoggable(Level)`/`log(Level, ...)` 的级别参数。
```java
log.log(System.Logger.Level.ERROR, "failed");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/System/Logger` 系列验证级别名称与严重度排序。

## 4. 测试用例设计 + 自动化实现
白盒：`Level.class.isEnum()`、继承 `Enum`。
黑盒：7 个常量、严重度严格递增、`getName`、`valueOf` 往返。
对应 `<Simple>Test.java` 黑盒部分。
