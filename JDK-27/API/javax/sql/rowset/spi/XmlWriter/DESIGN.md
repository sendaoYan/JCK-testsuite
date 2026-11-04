# javax.sql.rowset.spi.XmlWriter 设计案

## 1. API 功能

`XmlWriter` 是 XML 写出接口，继承 `javax.sql.RowSetWriter`（从而继承 `boolean writeData(RowSetInternal)`），用于把 `WebRowSet` 序列化为 XML 文本。

公开成员（依据 JDK-27 `javap`）：

- 继承自 `RowSetWriter`：`boolean writeData(RowSetInternal caller) throws SQLException`
- 本接口新增（1 个）：
  - `void writeXML(WebRowSet caller, Writer writer) throws SQLException`

（注意：本接口只有 `Writer` 重载，**没有** `OutputStream` 重载；`OutputStream` 写 XML 是 `WebRowSet.writeXml(OutputStream)` 的职责。）

## 2. Java 用户如何使用

实现方（如 `WebRowSetXmlWriter`）把 `WebRowSet` 内容写入 `Writer`；应用侧通常通过 `WebRowSet.writeXml(Writer)` 间接使用：

```java
XmlWriter writer = new XmlWriter() {
    public boolean writeData(RowSetInternal caller) { return true; }
    public void writeXML(WebRowSet caller, Writer w) throws SQLException { w.write("<rowset/>"); }
};
StringWriter out = new StringWriter();
writer.writeXML(webRowSet, out);
```

## 3. 开源代码实际用法调研

- JDK 实现 `com/sun/rowset/WebRowSetImpl.java`：内部持有 `WebRowSetXmlWriter xmlWriter`（实现 `XmlWriter`），在 `writeXml(Writer)` 中调用 `xmlWriter.writeXML(this, writer)`。
- `com/sun/rowset/internal/WebRowSetXmlWriter.java` 是 `XmlWriter` 的参考实现（`test/jdk` 无直接单测文件）。

## 4. 测试用例设计 + 自动化实现

`XmlWriterTest.java` 分两部分：

- **白盒**：接口形态（interface、public、非 enum/annotation、直接父接口 `javax.sql.RowSetWriter`、声明方法数 1）。
- **黑盒**：匿名实现组合操作断言：
  - `instanceof RowSetWriter`；
  - `writeData(null)` 返回 true；
  - `writeXML(null, StringWriter)` 被调用，且把固定 XML 内容写入 `Writer`，断言写出内容逐字一致。

自动化断言总数：12 个 check（白盒 + 黑盒）。
