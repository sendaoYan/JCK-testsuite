# java.awt.image.BandCombineOp 设计案

## 1. API 功能分组
- **类型**：BandCombineOp implements RasterOp。
- **构造**：`BandCombineOp(float[][] matrix, RenderingHints)`。
- **操作**：`filter(Raster,WritableRaster)`（按矩阵线性组合各 band）、`getMatrix()`、`getBounds2D/getPoint2D/createCompatibleDestRaster/getRenderingHints`。

## 2. Java 用户如何使用
用矩阵对栅格每个像素做 band 线性变换：`dst[i] = sum_j matrix[i][j]*src[j] + matrix[i][numBands]`。例如 `{ {1,0,0,10} }` 对红通道加 10。

## 3. 开源代码实际用法调研
- `java.awt.image.BandCombineOp` 是 RasterOp 标准实现（`src/java.desktop/share/classes/java/awt/image/BandCombineOp.java`）。
- `test/jdk/java/awt/image/BandCombineOp/` 用例验证矩阵过滤与边界（本用例 identity/offset 矩阵过滤参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`BandCombineOpTest.testBlackBox()`）：
1. 单位矩阵过滤后像素不变；带偏移矩阵加常量。
2. getMatrix 返回副本、getBounds2D==栅格 bounds、createCompatibleDestRaster/getPoint2D/getRenderingHints。
白盒（`testWhiteBox()`）：public、implements RasterOp、getMatrix/filter 签名。
