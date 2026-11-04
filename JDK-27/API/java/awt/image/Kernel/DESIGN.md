# java.awt.image.Kernel 设计案

## 1. API 功能分组
- 构造：`Kernel(int width, int height, float[] data)`。
- 查询：`getWidth/getHeight/getXOrigin/getYOrigin/getKernelData(float[])`。
- 派生：`clone()`（implements Cloneable）。

## 2. Java 用户用法
- 卷积核：`new Kernel(3, 3, new float[]{...})`，原点默认在中心 `(width/2, height/2)`。
- `getKernelData(arr)` 复制核数据；`clone()` 复制核对象。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `ConvolveOp` 用 `Kernel` 表达卷积核。
- `test/jdk/java/awt/image/ConvolveOp/` 构造 Kernel 并卷积。
- 图像处理库（锐化/模糊/边缘检测）用 Kernel 定义滤波器。

## 4. 测试设计（指向 KernelTest.java）
- 白盒：implements Cloneable、查询/克隆方法存在。
- 黑盒：尺寸/原点；`getKernelData` 复制与输入独立性（构造时拷贝）；`clone` 独立性；错误数据长度抛异常。
