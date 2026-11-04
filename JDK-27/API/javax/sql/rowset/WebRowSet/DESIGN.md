# javax.sql.rowset.WebRowSet 测试设计

## 1. API 功能
`WebRowSet` 扩展 `CachedRowSet`，为行集提供 XML 序列化/反序列化能力。实现类 `com.sun.rowset.WebRowSetImpl`。接口新增：

- 常量：`PUBLIC_XML_SCHEMA`、`SCHEMA_SYSID`、`INTERNAL_XML_SCHEMA`（后两者值为 null，用于确定 schema 校验）。
- XML 写出：`writeXml(ResultSet, OutputStream)`、`writeXml(ResultSet, Writer)`、`writeXml(OutputStream)`、`writeXml(Writer)`。
- XML 读入：`readXml(Reader)`、`readXml(InputStream)`。

## 2. Java 用户如何使用
```java
WebRowSet wrs = RowSetProvider.newFactory().createWebRowSet();
wrs.setMetaData(md); /* 内存填充同 CachedRowSet */
ByteArrayOutputStream out = new ByteArrayOutputStream();
wrs.writeXml(out);                      // 序列化为 XML
wrs.writeXml(new StringWriter());       // 也可写 Writer
WebRowSet wrs2 = RowSetProvider.newFactory().createWebRowSet();
wrs2.readXml(new ByteArrayInputStream(out.toByteArray()));   // 反序列化
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/webrowset/` 与源码，不凭记忆。

- `CommonWebRowSetTests.java`：验证 `writeXml(OutputStream)`/`readXml(InputStream)` 与 `writeXml(Writer)`/`readXml(Reader)` 往返后 `size()`、列名、数据值一致，以及 `writeXml(ResultSet, OutputStream)` 直接序列化原始 ResultSet。
- 源码 `WebRowSetImpl.java`：`writeXml` 生成带 `<webRowSet>` 根元素、含 `metadata` 与 `data` 段的 XML；`readXml` 用 SAX 解析并重建行集。

## 4. 测试用例设计 + 自动化实现
测试文件 `WebRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、父接口 `CachedRowSet`、声明方法、3 个 schema 常量）。
- 黑盒 `testBlackBox()`：impl 类名/instanceof；内存填 2 行后 `writeXml(OutputStream)` 生成非空 XML 且含列名/数据值，`readXml(InputStream)` 反序列化后 `size`/游标/数据一致；`writeXml(Writer)/readXml(Reader)` 往返一致；`writeXml(ResultSet, OutputStream)` 直接序列化。
- 覆盖场景：24 条断言。

自动化要点：XML 只用纯内存 ByteArray/String 流，不落盘不连库；纯 headless，不加 `@key`。
