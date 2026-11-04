# java.awt.Color 黑盒行为测试设计

## 1. API 功能
- 常量：`white/WHITE/lightGray/gray/darkGray/black/red/pink/orange/yellow/green/magenta/cyan/blue`（大小写成对同实例）。
- 构造：`(int,int,int)`、`(int,int,int,int)`、`(int)`（rgb，强制 opaque）、`(int,boolean)`、`(float,float,float)`、`(float,float,float,float)`、`(ColorSpace,float[],float)`。
- 查询：`getRed/getGreen/getBlue/getAlpha/getRGB`、`getColorSpace`、`getTransparency`、`getRGBComponents/getRGBColorComponents/getComponents/getColorComponents`。
- 变换：`brighter()`、`darker()`。
- 静态工具：`decode(String)`、`getColor(String)`、`HSBtoRGB`、`RGBtoHSB`、`getHSBColor`。
- 值语义：`equals`、`hashCode`、`toString`。

## 2. Java 用户如何使用
UI 颜色常量 + RGB/HSB 转换 + 反序列化（`decode`）+ 图形填充（`Graphics.setColor`/`Paint`）。
```java
g.setColor(new Color(255, 128, 0));
Color accent = Color.decode("#336699");
Color hover = accent.brighter();
float[] hsb = Color.RGBtoHSB(255, 0, 0, null);
```
组合链路：`decode` → 读分量 → `brighter/darker` → `equals` 比较 → `getRGB` 落地。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Color.java`：`brighter` 因子 `FACTOR=0.7`，纯黑 `brighter()`→(3,3,3)，纯红 `brighter()` 不变；`darker` 乘 0.7 向下取整；`(int,int,int,int)` 越界抛 `IllegalArgumentException`；`decode` 委托 `Integer.decode`；`toString`=`java.awt.Color[r=..,g=..,b=..]`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/dnd/ImageDecoratedDnDInOut/MyCursor.java`（`new Color(...)` + `getRGB`）、`test/jdk/java/awt/geom/HitTest/PathHitTest.java`（`new Color` 构造）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量 RGB 值与大小写同实例。
- int/float/单 int/int+alpha 各构造器 + 默认 alpha 255 + 越界抛 `IllegalArgumentException`。
- `getRGB` ARGB 精确值；`brighter/darker` 精确分量（含 alpha 保留）；`equals/hashCode/toString`。
- `decode` 十六进制/八进制/十进制 + 非法抛 `NumberFormatException`。
- `HSBtoRGB`/`RGBtoHSB`/`getHSBColor` 精确值（1e-6 容差）。
- 组件数组（`getRGBComponents` 等）长度与分量；`getColorSpace` sRGB；`getTransparency` 三分支。
- 组合链：`decode` → 分量 → `equals` → `getRGB`。
断言 97 项，纯数学精确断言（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：常量表精确值；float 0.5f→128；packed 0x89ABCDEF,false→getRGB 0xFFABCDEF；brighter/darker 精确；范围不变量；HSB 往返；decode+NFE；IAE 矩阵(300/-1/1.5f/-0.1f)；属性系统色；equals 含 alpha；hashCode==getRGB；序列化保 alpha；toString 精确。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
