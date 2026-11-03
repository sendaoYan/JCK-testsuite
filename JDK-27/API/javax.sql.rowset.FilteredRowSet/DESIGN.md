# javax.sql.rowset.FilteredRowSet 测试设计

## 1. API 功能
`FilteredRowSet` 扩展 `WebRowSet`（进而 `CachedRowSet`），在游标推进时按 `Predicate` 过滤可见行。实现类 `com.sun.rowset.FilteredRowSetImpl`。接口仅新增两个方法：

- `setFilter(Predicate)`：设置行过滤器。
- `getFilter()`：返回当前过滤器，未设置时为 null。

## 2. Java 用户如何使用
```java
FilteredRowSet frs = RowSetProvider.newFactory().createFilteredRowSet();
// 内存填充与 CachedRowSet 相同（setMetaData + insertRow）
frs.setFilter(new Predicate() {
    public boolean evaluate(RowSet rs) { return rs.getInt(1) >= 2; }
    public boolean evaluate(Object v, int c) { return c == 1 && ((Integer) v) >= 2; }
    public boolean evaluate(Object v, String c) { return "ID".equalsIgnoreCase(c) && ((Integer) v) >= 2; }
});
frs.beforeFirst();
while (frs.next()) { /* 只看到满足 evaluate(RowSet) 的行 */ }
frs.setFilter(null);   // 清除过滤
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/filteredrowset/`，不凭记忆。

- `FilteredRowSetTests.java`：验证 `getFilter()` 初始为 null、`setFilter(p)` 后 `getFilter().equals(p)`、`setFilter(null)` 清空；用 `PrimaryKeyFilter`（按主键区间、列索引/列名）和 `CityFilter`（按城市列表）验证过滤后可见主键集合。
- `PrimaryKeyFilter.java`/`CityFilter.java`：展示了 `Predicate` 三个 `evaluate` 方法的标准实现，其中迭代实际调用 `evaluate(RowSet)`。
- 源码 `FilteredRowSetImpl.java`：重写 `internalNext/internalPrevious`，逐行调用 `p.evaluate(this)` 决定可见性。

## 4. 测试用例设计 + 自动化实现
测试文件 `FilteredRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、父接口 `WebRowSet`、声明方法 2）。
- 黑盒 `testBlackBox()`：impl 类名/instanceof；`getFilter` 初始 null、`setFilter(null)`；内存填 3 行后未过滤顺序 1,2,3；`Predicate`（ID>=2）过滤后 2,3；按 NAME 前缀过滤后只剩 2；清除过滤后恢复 1,2,3。
- 覆盖场景：22 条断言。

自动化要点：过滤器匿名实现三个 `evaluate` 方法（迭代用 `evaluate(RowSet)`，其余两个为接口完整性）；纯内存不连库；纯 headless，不加 `@key`。
