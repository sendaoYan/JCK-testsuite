# javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext — DESIGN

## 1. API 功能
`AbstractRegionPainter.PaintContext` 是 protected 静态嵌套类，封装画家缓存的 Insets/画布尺寸/inverted/CacheMode/最大缩放因子。构造器：`(Insets, Dimension, boolean)`、`(Insets, Dimension, boolean, CacheMode, double, double)`；`maxH`/`maxV` < 1 抛 `IllegalArgumentException`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:497`（class）、538（构造器）。

## 2. Java 用户如何使用
由 `AbstractRegionPainter` 子类通过继承访问：`new PaintContext(new Insets(...), new Dimension(...), false)`，作为 `getPaintContext()` 的返回值供 `paint()` 使用。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:149`：`ctx = getPaintContext();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:497`：`protected static class PaintContext`。

## 4. 测试用例设计（黑盒部分，`PaintContextTest`，经继承降级）
通过 `Painter extends AbstractRegionPainter` 继承访问 protected 类型并调用构造器：
1. `(null, null, false)`、`(insets, dim, true)`、`(insets, dim, false, null, 1, 1)`、`(insets, dim, false, null, 2.5, 3.0)` 均可构造。
2. `maxH=0.5` 与 `maxV=0.5` 分别抛 `IllegalArgumentException`。
