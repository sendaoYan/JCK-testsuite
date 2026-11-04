# javax.imageio.plugins.jpeg.JPEGHuffmanTable — DESIGN

## 1. API 功能
`JPEGHuffmanTable` 封装单个 JPEG Huffman 表。方法分组：
- 内置常量：`StdDCLuminance`、`StdDCChrominance`、`StdACLuminance`、`StdACChrominance`。
- 构造器：`JPEGHuffmanTable(short[] lengths, short[] values)`（复制入参并校验合法性）。
- 查询：`getLengths()`、`getValues()`（均返回副本）、`toString()`。

## 2. Java 用户如何使用
用户一般直接用内置表：`JPEGHuffmanTable.StdDCLuminance`；或自建表 `new JPEGHuffmanTable(lengths, values)`，其中 `lengths[k]` 表示码长为 `k+1` 位的码字数，`values` 按码长递增排列。随后通过 `JPEGImageReadParam.setDecodeTables`/`JPEGImageWriteParam.setEncodeTables` 把表传给编解码器。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/jpeg/JPEGHuffmanTable.java`：内置表来自 ITU T.81 附录 K.3.3；`validate()` 校验 `sum(lengths) == values.length`。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageReader.java` / `JPEGImageWriter.java`：解码/编码时读取 `getDCHuffmanTables()/getACHuffmanTables()` 构建默认 Huffman 表。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/jpeg/` 多处用 `JPEGHuffmanTable.Std*` 构造编码/解码参数。

## 4. 测试用例设计 + 自动化实现（`JPEGHuffmanTableTest.testBlackBox`）
- 四个内置表非空且 `sum(lengths)==values.length`，`toString` 前缀与分段校验。
- DC/AC 亮度表具体尺寸（lengths=16，values 分别为 12/162）。
- 构造器复制语义：入参往返一致，`getLengths/getValues` 返回副本（外部改动不影响内部）。
- 非法入参：null 数组、空数组、lengths 与 values 数量不匹配、负数长度均抛 `IllegalArgumentException`。
