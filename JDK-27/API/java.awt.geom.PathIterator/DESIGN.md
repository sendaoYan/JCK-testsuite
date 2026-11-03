# java.awt.geom.PathIterator 设计案

## 1. API 功能分组
- **常量**：缠绕规则 `WIND_EVEN_ODD=0`/`WIND_NON_ZERO=1`；segment 类型 `SEG_MOVETO=0`/`SEG_LINETO=1`/`SEG_QUADTO=2`/`SEG_CUBICTO=3`/`SEG_CLOSE=4`。
- **迭代协议**：`isDone()`/`next()`/`currentSegment(float[]|double[])`。
- **查询**：`getWindingRule()`。

## 2. Java 用户如何使用
从 `Shape.getPathIterator(at)` 取得迭代器，`while (!it.isDone()) { int t = it.currentSegment(coords); ...; it.next(); }` 提取几何轮廓，供绘制、填充、碰撞检测复用；曲线段返回控制点坐标（QUAD 2 点、CUBIC 3 点）。

## 3. 开源代码实际用法调研
- `java.awt.geom.Area` 用 PathIterator 遍历源 Shape 的轮廓构造区域（JDK 内部）。
- `java.awt.Graphics2D.draw/fill` 的渲染管线通过 PathIterator 把 Shape 转换为绘制指令（JDK 内部）。
- `test/jdk/java/awt/geom/Path2D/` 系列（如 `Path2DCopyConstructor`、`Path2DGrow`）用 PathIterator 断言 segment 序列与坐标（遍历范式与本用例一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`PathIteratorTest.testBlackBox()`）：
1. 7 个常量精确值（规范定义，跨平台稳定）。
2. 三角形路径 `moveTo/lineTo/lineTo/close` 遍历得到 MOVETO,LINETO,LINETO,CLOSE 序列，默认 WIND_NON_ZERO。
3. `currentSegment(float[])` 与 `currentSegment(double[])` 分别填充对应类型坐标。
4. 匿名实现接口验证协议可被自定义实现。
白盒（`testWhiteBox()`）：interface、7 个 public static final int 常量、5 个抽象方法签名。
