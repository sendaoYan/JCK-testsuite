# java.awt.image.ByteLookupTable 设计案

## 1. API 功能分组
- 构造：`ByteLookupTable(int offset, byte[][])`、`ByteLookupTable(int offset, byte[])`。
- 查询：`getTable`（继承 `getNumComponents/getOffset`）。
- 查表：`lookupPixel(int[],int[])`、`lookupPixel(byte[],byte[])`。

## 2. Java 用户用法
- 3 波段：`new ByteLookupTable(0, byte[][]{r,g,b})`，每表长 256；`lookupPixel(int[]{...})` 按波段映射。
- 单波段：`new ByteLookupTable(0, byte[])` 应用到所有通道。
- offset 使索引先减 offset 再查表。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `LookupOp` 对 byte 数据用 `ByteLookupTable` 查表。
- `test/jdk/java/awt/image/LookupOp/` 间接覆盖。
- 图像库用 ByteLookupTable 实现 8 位 LUT 映射。

## 4. 测试设计（指向 ByteLookupTableTest.java）
- 白盒：extends LookupTable、lookupPixel 方法存在。
- 黑盒：3 波段表 `lookupPixel(int[])` 精确映射；单波段应用全部通道；`lookupPixel(byte[])`；offset 位移索引。
