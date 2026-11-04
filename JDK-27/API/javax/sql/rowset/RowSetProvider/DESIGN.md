# javax.sql.rowset.RowSetProvider 测试设计

## 1. API 功能
`RowSetProvider` 是创建 `RowSetFactory` 的服务提供者工具类（具体类，无父类、无声明接口）。它通过 `ServiceLoader` 加载 `javax.sql.rowset.RowSetFactory` 的 SPI 实现，提供两个静态工厂：

- `static RowSetFactory newFactory()`：用系统默认类加载器。
- `static RowSetFactory newFactory(String factoryClassName, ClassLoader cl)`：指定实现类名与类加载器，找不到时抛 SQLException。

## 2. Java 用户如何使用
```java
RowSetFactory f = RowSetProvider.newFactory();
CachedRowSet crs = f.createCachedRowSet();
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/` 与源码，不凭记忆。

- 各测试统一 `rowSetFactory() = RowSetProvider.newFactory()`，再 `createXxxRowSet()`。
- 源码 `RowSetProvider.java`：`newFactory()` 用 `Thread.currentThread().getContextClassLoader()` 经 ServiceLoader 定位 `META-INF/services/javax.sql.rowset.RowSetFactory`（实际为 `com.sun.rowset.RowSetFactoryImpl`）；`newFactory(String, ClassLoader)` 找不到指定类时抛 `SQLException`。

## 4. 测试用例设计 + 自动化实现
测试文件 `RowSetProviderTest.java`：
- 白盒 `testWhiteBox()`：反射断言（具体类、无声明接口、静态方法 2）。
- 黑盒 `testBlackBox()`：`newFactory()` 非 null 且返回 `RowSetFactoryImpl`；`newFactory(null, cl)` / `newFactory("不存在的类", cl)` 抛 SQLException；`newFactory("com.sun.rowset.RowSetFactoryImpl", cl)` 成功；多次 `newFactory()` 返回可用工厂并可 `createCachedRowSet()`。
- 覆盖场景：19 条断言。

自动化要点：不连接数据库；纯 headless，不加 `@key`。
