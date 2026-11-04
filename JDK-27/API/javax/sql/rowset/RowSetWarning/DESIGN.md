# javax.sql.rowset.RowSetWarning 测试设计

## 1. API 功能
`RowSetWarning` 是 RowSet 操作的警告异常类，继承 `java.sql.SQLException`（注意：不是 `SQLWarning`）。提供 4 个构造器：

- `RowSetWarning()`、`RowSetWarning(String)`、`RowSetWarning(String, String)`、`RowSetWarning(String, String, int)`。

## 2. Java 用户如何使用
```java
RowSetWarning w = new RowSetWarning("msg", "SQLState", 42);
w.getMessage(); w.getSQLState(); w.getErrorCode();
Throwable cause = w.getCause();   // SQLException 链式语义
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/src/java.sql.rowset/share/classes/javax/sql/rowset/RowSetWarning.java` 源码，不凭记忆。

- 源码：4 个构造器均委托 `super(...)` 到 `SQLException` 对应构造器，`getMessage/getSQLState/getErrorCode` 继承自 `SQLException`。

## 4. 测试用例设计 + 自动化实现
测试文件 `RowSetWarningTest.java`：
- 白盒 `testWhiteBox()`：反射断言（具体类、父类 `SQLException`、声明构造器 4 个）。
- 黑盒 `testBlackBox()`：无参/单参/双参/三参构造器后 `getMessage/getSQLState/getErrorCode/getCause` 校验；`instanceof SQLException` 为 true；`initCause` 链式语义。
- 覆盖场景：20 条断言。

自动化要点：纯异常对象测试，无 DB、无 IO；纯 headless，不加 `@key`。
