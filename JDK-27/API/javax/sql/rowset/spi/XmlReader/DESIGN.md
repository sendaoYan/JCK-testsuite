# javax.sql.rowset.spi.XmlReader 设计案

## 1. API 功能

`XmlReader` 是 XML 读取接口，继承 `javax.sql.RowSetReader`（从而继承 `void readData(RowSetInternal)`），用于把 XML 文本读入 `WebRowSet`。

公开成员（依据 JDK-27 `javap`）：

- 继承自 `RowSetReader`：`void readData(RowSetInternal caller) throws SQLException`
- 本接口新增（1 个）：
  - `void readXML(WebRowSet caller, Reader reader) throws SQLException`

（注意：本接口只有 `Reader` 重载，**没有** `InputStream` 重载；`InputStream` 读 XML 是 `WebRowSet.readXml(InputStream)` 的职责。）

## 2. Java 用户如何使用

实现方（如 `WebRowSetXmlReader`）从 `Reader` 解析 XML 并填充 `WebRowSet`；应用侧通常通过 `WebRowSet.readXml(Reader)` 间接使用：

```java
XmlReader reader = new XmlReader() {
    public void readData(RowSetInternal caller) { }
    public void readXML(WebRowSet caller, Reader r) throws SQLException { /* parse */ }
};
reader.readXML(webRowSet, new StringReader("<rowset>...</rowset>"));
```

## 3. 开源代码实际用法调研

- JDK 实现 `com/sun/rowset/WebRowSetImpl.java`：内部持有 `WebRowSetXmlReader xmlReader`（实现 `XmlReader`），在 `readXml(Reader)` 中调用 `xmlReader.readXML(this, reader)`。
- `com/sun/rowset/internal/WebRowSetXmlReader.java` 是 `XmlReader` 的参考实现（`test/jdk` 无直接单测文件）。

## 4. 测试用例设计 + 自动化实现

`XmlReaderTest.java` 分两部分：

- **白盒**：接口形态（interface、public、非 enum/annotation、直接父接口 `javax.sql.RowSetReader`、声明方法数 1）。
- **黑盒**：匿名实现组合操作断言：
  - `instanceof RowSetReader`；
  - `readData(null)` 可调用；
  - `readXML(null, StringReader(xml))` 被调用，且把 Reader 内容原样消费进缓冲区（逐块 `read` 循环），断言逐字一致。

自动化断言总数：11 个 check（白盒 + 黑盒）。
