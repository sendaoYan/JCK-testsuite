# javax.imageio.plugins.jpeg.JPEGQTable — DESIGN

## 1. API 功能
`JPEGQTable` 封装 8x8 JPEG 量化表（自然序，非 zig-zag）。方法分组：
- 内置常量：`K1Luminance`、`K1Div2Luminance`、`K2Chrominance`、`K2Div2Chrominance`。
- 构造器：`JPEGQTable(int[] table)`（需 64 元素，复制入参）。
- 查询/变换：`getTable()`（返回副本）、`getScaledInstance(float, boolean)`、`toString()`。

## 2. Java 用户如何使用
用户用内置表或自建 64 元素 `int[]`，`new JPEGQTable(arr)` 后经 `getScaledInstance(scaleFactor, forceBaseline)` 生成质量缩放表；再传给 `JPEGImageReadParam.setDecodeTables` / `JPEGImageWriteParam.setEncodeTables`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/jpeg/JPEGQTable.java`：内置表来自 JPEG 规范 K.1/K.2；`getScaledInstance` 把每个元素乘 `scaleFactor` 加 0.5 后截断，并 clamp 到 `[1,32767]`（`forceBaseline` 时 `[1,255]`）。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageWriter.java`：写 JPEG 时依据压缩质量从默认量化表缩放生成实际表。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/jpeg/` 有压缩质量往返测试。

## 4. 测试用例设计 + 自动化实现（`JPEGQTableTest.testBlackBox`）
- 四个内置表非空且 64 元素、`toString` 前缀。
- 构造器复制语义：入参往返、`getTable()` 返回副本、构造后改入参不影响内部。
- `getScaledInstance`：`1.0f` 精确保留、`2.0f` 每项翻倍、`forceBaseline` clamp `[1,255]`、非 baseline clamp `[1,32767]`。
- 非法入参：null、长度 63/65 均抛 `IllegalArgumentException`。
