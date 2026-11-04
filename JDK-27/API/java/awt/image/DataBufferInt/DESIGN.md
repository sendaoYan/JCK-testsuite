# java.awt.image.DataBufferInt 设计案

## 1. API 功能分组
- 构造：`DataBufferInt(int)`、`(int,int)`、`(int[],int)`、`(int[],int,int)`、`(int[][],int)`、`(int[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：`getElem(int)/getElem(int,int)`、`setElem(int,int)/setElem(int,int,int)`。

## 2. Java 用户用法
- `new DataBufferInt(8)` 分配 8 个 int；`setElem(i,v)`/`getElem(i)`。
- 外部数组共享：`new DataBufferInt(int[], size)`，`getData()` 返回同一数组，写操作影响原数组。
- 多 bank：`new DataBufferInt(int[][], size)` 按 bank 存取。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `BufferedImage(TYPE_INT_ARGB/RGB)` 底层 `Raster` 默认使用 `DataBufferInt` 存 ARGB 像素。
- JDK 内部 `DirectColorModel` 兼容栅格用 int 传输类型。
- 游戏/图像库直接 `getData()` 操作 int 像素数组。

## 4. 测试设计（指向 DataBufferIntTest.java）
- 白盒：final、extends DataBuffer、关键方法存在。
- 黑盒：类型/尺寸/offset/bank；`setElem/getElem` 往返（含 int 极值、负数）；数组构造共享底层并 `setElem` 写回；多 bank 读写与 `getBankData`。
