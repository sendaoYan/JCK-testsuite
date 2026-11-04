# java.awt.image.DataBuffer 设计案

## 1. API 功能分组
- 常量：`TYPE_BYTE/USHORT/SHORT/INT/FLOAT/DOUBLE/UNDEFINED`，`getDataTypeSize(int)`。
- 查询：`getDataType/getSize/getOffset/getOffsets/getNumBanks`。
- 元素读写：`getElem(int)/getElem(int,int)`、`setElem(int,int)/setElem(int,int,int)`、`getElemFloat/getElemDouble/setElemFloat/setElemDouble`（int 与带 bank 两种）。

## 2. Java 用户用法
- 抽象基类，不直接实例化；用子类 `DataBufferInt` 等存储像素。
- `db.getSize()`、`db.getElem(i)`、`db.setElem(i,v)`；多 bank 用 `getElem(i,bank)`。
- 静态 `DataBuffer.getDataTypeSize(TYPE_INT)` 返回位宽。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `Raster/SampleModel` 全部通过 `DataBuffer` 存取样本。
- `test/jdk/java/awt/image/Raster/`、`GetSamplesTest.java` 通过 `raster.getDataBuffer()` 拿 DataBuffer 采样。
- 图像处理库对 `DataBufferInt/Byte` 直接访问底层数组做高性能像素操作。

## 4. 测试设计（指向 DataBufferTest.java）
- 白盒：abstract、常量、静态/实例方法存在。
- 黑盒：`getDataTypeSize` 各位宽与越界异常；用 `DataBufferInt` 验证 `getSize/getOffset/getNumBanks/getElem/setElem`、浮点/双精度读写截断、多 bank 派发。
