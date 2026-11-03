# java.awt.font.LayoutPath 设计案

## 1. API 功能分组
- **抽象类**：`LayoutPath`（无公开构造）。
- **映射**：`pointToPath(Point2D loc, Point2D dst)` 把点映射到路径上（返回是否在路径上下方）；`pathToPoint(Point2D loc, boolean preceding, Point2D dst)` 把路径上的点映射回（preceding 决定上/下方偏移）。

## 2. Java 用户如何使用
供曲线文本排版：`TextLayout.getLayoutPath()` 返回路径，用户实现 LayoutPath 定义文本沿曲线（如圆形）排列的坐标映射；`pointToPath` 把用户空间点投影到路径，`pathToPoint` 反向展开。

## 3. 开源代码实际用法调研
- `TextLayout.getLayoutPath()` 返回该抽象类型（`src/java.desktop/share/classes/java/awt/font/TextLayout.java`）。
- JDK 内部 `sun.font` 有 LayoutPath 的具体实现用于沿线文本（源码 `src/java.desktop/share/classes/sun/font/`）。
- `test/jdk/java/awt/font/TextLayout/` 曲线文本用例覆盖 LayoutPath 映射（本用例 pass-through 匿名实现参考其基本映射语义）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`LayoutPathTest.testBlackBox()`，匿名实现水平基线投影）：
1. pointToPath 投影到基线并返回上下方标志。
2. pathToPoint preceding=true/false 分别映射到基线上/下方。
白盒（`testWhiteBox()`）：abstract public class、两个映射方法签名。
