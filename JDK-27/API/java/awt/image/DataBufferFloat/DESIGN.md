# java.awt.image.DataBufferFloat 设计案

## 1. API 功能分组
- 构造：`DataBufferFloat(int)`、`(int,int)`、`(float[],int)`、`(float[],int,int)`、`(float[][],int)`、`(float[][],int,int[])`。
- 数据访问：`getData()/getData(int)/getBankData()`。
- 元素读写：int 型 `getElem/setElem` 与浮点 `getElemFloat/setElemFloat`、`getElemDouble/setElemDouble`（各含带 bank 版本）。

## 2. Java 用户用法
- `new DataBufferFloat(8)`；`setElemFloat(i, 2.5f)`/`getElemFloat(i)`。
- `setElem(i,v)` 以 int 存入、`getElem(i)` 截断为 int；`setElemDouble/getElemDouble` 提供双精度接口。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 TYPE_FLOAT 样本栅格（如浮点高程/科学图像）使用 `DataBufferFloat`。
- 图像处理库对 HDR/浮点图像用 `getElemFloat/setElemFloat` 保持精度。
- `SampleModel` 浮点样本路径经由 DataBufferFloat。

## 4. 测试设计（指向 DataBufferFloatTest.java）
- 白盒：final、extends DataBuffer、浮点方法存在。
- 黑盒：类型/尺寸/offset/bank；`setElemFloat/getElemFloat` 往返、`getElem` 截断、`setElemDouble/getElemDouble`、`setElem(int)` 存储为 float；数组构造与多 bank 读写。
