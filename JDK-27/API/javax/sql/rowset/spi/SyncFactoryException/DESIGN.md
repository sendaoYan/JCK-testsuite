# javax.sql.rowset.spi.SyncFactoryException 设计案

## 1. API 功能

`SyncFactoryException` 是 `SyncFactory` SPI 操作失败时抛出的受检异常，继承 `java.sql.SQLException`。

公开成员（依据 JDK-27 `javap`）：

- 构造器：
  - `SyncFactoryException()`
  - `SyncFactoryException(String msg)`

（注意：**没有** 三参 `(String, String, int)` 构造器，也没有其它重载。）

继承自 `SQLException` 的能力：`getMessage`、`getSQLState`、`getErrorCode`、`initCause`/`getCause`、`setNextException`/`getNextException`（SQLException 迭代链）。

## 2. Java 用户如何使用

```java
try {
    SyncFactory.getInstance(null);            // 或其它 SPI 调用
} catch (SyncFactoryException e) {
    String msg = e.getMessage();
    e.initCause(new IllegalStateException("root"));
    e.setNextException(new SyncFactoryException("next"));
    Throwable cause = e.getCause();
}
```

## 3. 开源代码实际用法调研

- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/spi/SyncFactoryExceptionTests.java`：
  - `test01`/`test02`：无参/带消息构造后 `getMessage`/`getSQLState`/`getCause`/`getErrorCode` 的默认值断言（message 为 null 或指定值，SQLState/cause 为 null，errorCode 为 0）；
  - `test03`/`test04`：`initCause` + `setNextException` 后按 for-each / while 遍历 `SyncFactoryException` 的异常链顺序。
- JDK 实现 `SyncFactory.java` 中 `getInstance(null)`、`getLogger()`（未设置日志）、`initMapIfNecessary()`（找不到 `rowset.properties`）等处抛出此异常。

## 4. 测试用例设计 + 自动化实现

`SyncFactoryExceptionTest.java` 分两部分：

- **白盒**：类形态（类、public、父类 `java.sql.SQLException`、声明构造器数 2、可赋值给 SQLException）。
- **黑盒**：
  - 无参构造：message/SQLState 为 null、errorCode 0、`instanceof SQLException`。
  - `String` 构造：message 精确匹配。
  - 继承链路：`initCause` 返回 this 且 `getCause` 取回；`setNextException`/`getNextException` 往返。
  - 抛出/捕获：作为 `SyncFactoryException` 与 `SQLException` 捕获。

自动化断言总数：15 个 check（白盒 + 黑盒）。
