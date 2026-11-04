# javax.sql.rowset.Predicate 测试设计

## 1. API 功能
`Predicate` 是 `FilteredRowSet` 使用的行过滤器接口（无父接口、无常量），声明 3 个 `evaluate` 方法：

- `boolean evaluate(RowSet rs)`：按整个行集当前行判断可见性（迭代时实际调用）。
- `boolean evaluate(Object value, int column)`：按列索引判断。
- `boolean evaluate(Object value, String columnName)`：按列名判断。

## 2. Java 用户如何使用
```java
Predicate p = new Predicate() {
    public boolean evaluate(RowSet rs) { return rs.getInt(1) >= 2; }
    public boolean evaluate(Object v, int c) { return c == 1 && ((Integer) v) >= 2; }
    public boolean evaluate(Object v, String c) { return "ID".equalsIgnoreCase(c) && ((Integer) v) >= 2; }
};
FilteredRowSet frs = RowSetProvider.newFactory().createFilteredRowSet();
frs.setFilter(p);
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/filteredrowset/`，不凭记忆。

- `PrimaryKeyFilter.java`：按主键区间过滤，`evaluate(RowSet)` 用 `rs.getInt(loCol)` 判定；`evaluate(Object,int)` 用 `((Integer) value).intValue()` 落在 [lo,hi] 内；`evaluate(Object,String)` 按列名解析列号。
- `CityFilter.java`：按城市名单过滤，`evaluate(RowSet)` 用 `rs.getString(col)` 与 `cities.contains` 判定。
- 源码 `FilteredRowSetImpl.java`：游标推进时对每行调用 `p.evaluate(this)`，即三个方法中只有 `evaluate(RowSet)` 参与真实迭代过滤。

## 4. 测试用例设计 + 自动化实现
测试文件 `PredicateTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、无父接口、声明方法 3）。
- 黑盒 `testBlackBox()`：匿名实现 `Predicate` 验证三个 `evaluate` 方法的行为（索引/列名/行集判定），含 `evaluate(null)` 空安全返回 false、非法列索引/列名返回 false；再用真实 `FilteredRowSetImpl` 内存填 3 行后 `setFilter` 验证过滤后仅剩满足 `ID>=2` 的行。
- 覆盖场景：15 条断言。

自动化要点：匿名 `Predicate.evaluate(RowSet)` 需判空（避免 `rs == null` NPE）；纯内存不连库；纯 headless，不加 `@key`。
