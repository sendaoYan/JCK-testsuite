# java.awt.image.DataBufferUShort 设计案

## 1. API 功能分组
- 构造：`DataBufferUShort(int)`、`(int,int)`、`(short[],int)`、`(short[],int,int)`、`(short[][],int)`、`(short[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：`getElem(int)/getElem(int,int)`、`setElem(int,int)/setElem(int,int,int)`。

## 2. Java 用户用法
- `new DataBufferUShort(8)`；`setElem(i,65535)`/`getElem(i)` 无符号读（底层 short 按 0–65535 解释，-1→65535）。
- 外部数组共享与多 bank 读写同 DataBufferShort 模式。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 16 位无符号样本栅格（TYPE_USHORT）使用 `DataBufferUShort`。
- 遥感/医学图像（16 位无符号灰度）常用该类型。
- `SinglePixelPackedSampleModel` 对 TYPE_USHORT 的 16 位打包像素使用该缓冲。

## 4. 测试设计（指向 DataBufferUShortTest.java）
- 白盒：final、extends DataBuffer、方法存在。
- 黑盒：类型/尺寸/offset/bank；无符号读（65535、-1→65535）；数组构造共享底层；多 bank 读写。
