# java.awt.image.ShortLookupTable 设计案

## 1. API 功能分组
- 构造：`ShortLookupTable(int offset, short[][])`、`ShortLookupTable(int offset, short[])`。
- 查询：`getTable`（继承 `getNumComponents/getOffset`）。
- 查表：`lookupPixel(int[],int[])`、`lookupPixel(short[],short[])`。

## 2. Java 用户用法
- 16 位样本：`new ShortLookupTable(0, short[][]{...})`，每表长 65536；`lookupPixel(int[])` 输入按 `& 0xffff` 取低 16 位索引。
- 单波段应用到所有通道；offset 先减后查。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `LookupOp` 对 ushort/short 数据用 `ShortLookupTable`。
- `test/jdk/java/awt/image/LookupOp/` 间接覆盖。
- 图像库用 ShortLookupTable 实现 16 位 LUT 映射。

## 4. 测试设计（指向 ShortLookupTableTest.java）
- 白盒：extends LookupTable、lookupPixel 方法存在。
- 黑盒：3 波段表 `lookupPixel(int[])` 精确映射；单波段应用全部通道与 `& 0xffff` 掩码；`lookupPixel(short[])`；offset 位移索引。
