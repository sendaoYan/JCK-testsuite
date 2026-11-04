# java.awt.font.FontRenderContext 设计案

## 1. API 功能分组
- **构造**：`FontRenderContext(AffineTransform tx, boolean isAntiAliased, boolean usesFractionalMetrics)`；`FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint)`。
- **查询**：`getTransform()`（返回防御性副本）、`getTransformType()`、`isTransformed()`、`isAntiAliased()`、`usesFractionalMetrics()`、`getAntiAliasingHint()`、`getFractionalMetricsHint()`。
- **工具**：`equals/hashCode`。

## 2. Java 用户如何使用
字体渲染度量需要渲染上下文：`FontRenderContext frc = g2d.getFontRenderContext();`（Graphics2D 提供），或 `new FontRenderContext(identity, true, false)` 自建。随后交给 `Font.getLineMetrics/getStringBounds/createGlyphVector`、`TextLayout` 构造器。

## 3. 开源代码实际用法调研
- `java.awt.Graphics2D.getFontRenderContext()` 是标准来源（`src/java.desktop/share/classes/java/awt/Graphics2D.java` 接口方法）。
- `test/jdk/java/awt/font/` 系列（如 `TextLayout`、`GlyphVector` 用例）大量用 `new FontRenderContext(identity, antialias, fractional)` 构造固定度量环境（本用例 FRC 构造范式一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`FontRenderContextTest.testBlackBox()`）：
1. 布尔构造器 aa/fm 判定；非空 tx 的 isTransformed 与 getTransformType。
2. getTransform 返回独立副本（两次调用不同实例但相等）。
3. Object-hint 构造器：hint ON 时 isAntiAliased/usesFractionalMetrics/get*Hint。
4. 缩放变换的 transformType；equals/hashCode 契约。
白盒（`testWhiteBox()`）：public class、7 个查询方法签名。
