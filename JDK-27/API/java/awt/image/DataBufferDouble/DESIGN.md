# java.awt.image.DataBufferDouble 设计案

## 1. API 功能分组
- 构造：`DataBufferDouble(int)`、`(int,int)`、`(double[],int)`、`(double[],int,int)`、`(double[][],int)`、`(double[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：int 型 `getElem/setElem` 与浮点 `getElemFloat/setElemFloat`、`getElemDouble/setElemDouble`（各含带 bank 版本）。

## 2. Java 用户用法
- `new DataBufferDouble(8)`；`setElemDouble(i, 2.5)`/`getElemDouble(i)`。
- `setElem(i,v)` 以 int 存入、`getElem(i)` 截断为 int；`getElemFloat/setElemFloat` 提供单精度接口。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 TYPE_DOUBLE 样本栅格（双精度科学/遥感图像）使用 `DataBufferDouble`。
- 图像处理库对双精度浮点图像用 `getElemDouble/setElemDouble` 保持精度。
- `SampleModel` 双精度样本路径经由 DataBufferDouble。

## 4. 测试设计（指向 DataBufferDoubleTest.java）
- 白盒：final、extends DataBuffer、双精度方法存在。
- 黑盒：类型/尺寸/offset/bank；`setElemDouble/getElemDouble` 往返、`getElem` 截断、`setElemFloat/getElemFloat`、`setElem(int)` 存储为 double；数组构造与多 bank 读写。
