# java.awt.geom.FlatteningPathIterator 设计案

## 1. API 功能分组
- **构造**：`FlatteningPathIterator(PathIterator, double flatness)` 与 `FlatteningPathIterator(PathIterator, double flatness, int limit)`。
- **查询**：`getFlatness()`、`getRecursionLimit()`、`getWindingRule()`（透传源 PathIterator 的缠绕规则）。
- **迭代**（实现 PathIterator）：`isDone()` / `next()` / `currentSegment(float[]|double[])`，把源路径中的曲线段（QUADTO/CUBICTO）用直线段逼近输出。

## 2. Java 用户如何使用
把任意带曲线的 `PathIterator` 喂给 `FlatteningPathIterator` 后，遍历时只得到 SEG_MOVETO/SEG_LINETO/SEG_CLOSE，用于绘制或碰撞检测前的曲线近似：`for (FlatteningPathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.5); !it.isDone(); it.next()) { int t = it.currentSegment(coords); ... }`。flatness 越小越逼近原曲线，limit 控制递归细分深度上限。

## 3. 开源代码实际用法调研
- `java.awt.geom.Area`（`src/java.desktop/share/classes/java/awt/geom/Area.java`）内部用 `FlatteningPathIterator` 把曲线区域多边形化（真实 JDK 用法）。
- `sun.java2d.pipe.LoopPipe` / 渲染管线用 flattening 把曲线转直线供扫描线填充（JDK 内部）。
- `test/jdk/java/awt/geom/Path2D/` 系列测试用 PathIterator 遍历断言 segment 类型与坐标（遍历范式与本用例一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`FlatteningPathIteratorTest.testBlackBox()`）：
1. 对含 `quadTo` 的曲线路径做 flattening，断言 `getFlatness`/`getRecursionLimit`/`getWindingRule`。
2. 遍历输出只含直线类 segment、绝无 QUAD/CUBIC，且段数 >= 5。
3. 显式 limit 构造器：flatness=0.5、limit=3 读回正确。
4. 已平直的矩形路径透传为 1 moveTo + 3 lineTo + 1 close。
5. 负 flatness 构造抛 IllegalArgumentException（flatness must be >= 0）。
6. flatness=0 时细分到递归上限：默认 limit=10 恰好 2^10+1=1025 段。
7. winding rule 从被包装迭代器透传；迭代中 getFlatness 不超过请求的 flatness（实测 max==请求值）。
白盒（`testWhiteBox()`）：public class、implements PathIterator、构造与 5 个实例方法签名存在。
