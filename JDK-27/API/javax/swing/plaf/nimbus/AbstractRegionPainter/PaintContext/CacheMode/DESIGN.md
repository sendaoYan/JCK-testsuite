# javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext.CacheMode — DESIGN

## 1. API 功能
`PaintContext.CacheMode` 是 `AbstractRegionPainter.PaintContext` 的 protected 静态嵌套枚举，控制画家缓存策略：`NO_CACHING`/`FIXED_SIZES`/`NINE_SQUARE_SCALE`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:501`（enum 声明，作为 `PaintContext` 的 protected 嵌套类型）。

## 2. Java 用户如何使用
外部默认包代码无法直接命名（protected 且父类路径深），通常通过 `AbstractRegionPainter` 子类的 `PaintContext` 构造器传入；JDK 内部 `paint()` 按 `ctx.cacheMode` 分支（见 `AbstractRegionPainter.java:150`）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:150`：`PaintContext.CacheMode cacheMode = ctx == null ? NO_CACHING : ctx.cacheMode;`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:156`：`cacheMode == FIXED_SIZES` 分支。

## 4. 测试用例设计（黑盒部分，`CacheModeTest.testBlackBox`，反射降级）
因 protected 枚举不可直接命名，黑盒通过 `Class.forName` 反射调用 `values()`/`valueOf()`：
1. `values()` 长度 3，顺序 NO_CACHING/FIXED_SIZES/NINE_SQUARE_SCALE，ordinal 0/1/2。
2. `valueOf` 各名返回与 `values()` 同一实例。
3. `name() == toString()`；`valueOf("BOGUS")` 抛 `IllegalArgumentException`。
