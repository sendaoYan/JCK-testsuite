# java.awt.image.DataBufferShort 设计案

## 1. API 功能分组
- 构造：`DataBufferShort(int)`、`(int,int)`、`(short[],int)`、`(short[],int,int)`、`(short[][],int)`、`(short[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：`getElem(int)/getElem(int,int)`、`setElem(int,int)/setElem(int,int,int)`。

## 2. Java 用户用法
- `new DataBufferShort(8)` 分配 8 个 short；有符号存取 `setElem(i,-12345)`/`getElem(i)`。
- 外部数组共享与多 bank 读写同 DataBufferInt 模式。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 16 位有符号样本栅格（TYPE_SHORT）使用 `DataBufferShort`。
- 医学/遥感图像处理中 16 位有符号灰度数据常用该类型。
- `SampleModel` 的 `getSample/setSample` 对 TYPE_SHORT 数据路径经由 DataBufferShort。

## 4. 测试设计（指向 DataBufferShortTest.java）
- 白盒：final、extends DataBuffer、方法存在。
- 黑盒：类型/尺寸/offset/bank；有符号 short 往返（负值、32767）；数组构造共享底层；多 bank 读写。
