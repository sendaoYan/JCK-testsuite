# java.awt.image.DataBufferByte 设计案

## 1. API 功能分组
- 构造：`DataBufferByte(int size)`、`(int,int)`、`(byte[],int)`、`(byte[],int,int)`、`(byte[][],int)`、`(byte[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：`getElem(int)/getElem(int,int)`、`setElem(int,int)/setElem(int,int,int)`。

## 2. Java 用户用法
- `new DataBufferByte(8)` 分配 8 个字节；`setElem(i,v)` 存、`getElem(i)` 无符号读（`byte` 转 0–255）。
- 用外部数组构造共享底层：`new DataBufferByte(arr, size)`，`getData()` 返回同一数组。
- 多 bank：`new DataBufferByte(byte[][], size)`，`getElem(i,bank)` 指定 bank。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 8 位像素栅格（如 `ComponentSampleModel` TYPE_BYTE）默认用 `DataBufferByte`。
- `test/jdk/java/awt/image/GetSamplesTest.java` 间接使用 byte 样本读取。
- 图像库直接取 `getData()` 做灰度/调色板像素处理。

## 4. 测试设计（指向 DataBufferByteTest.java）
- 白盒：final、extends DataBuffer、getData/getElem/setElem 存在。
- 黑盒：类型/尺寸/offset/bank 数；`setElem/getElem` 往返与无符号读（-56→200）；数组构造共享底层并 `setElem` 写回；多 bank 读写与 `getData(int)`。
