# javax.swing.border.MatteBorder 设计案

## 1. API 功能
`MatteBorder` 继承 `EmptyBorder`，用纯色或平铺图标填充边框。方法分组：

- 构造：`MatteBorder(int top, int left, int bottom, int right, Color)`；`MatteBorder(Insets, Color)`；`MatteBorder(int top, int left, int bottom, int right, Icon)`；`MatteBorder(Insets, Icon)`；`MatteBorder(Icon)`（边距按图标宽高动态计算）。
- 绘制：`paintBorder(...)`（颜色填充或图标平铺边缘）。
- 边距：`getBorderInsets(Component, Insets)` 与 `getBorderInsets()`（图标构造时用 `getIconWidth()/getIconHeight()` 推导，否则用指定值）。
- 查询：`getMatteColor()`（纯色时返回色，图标时为 null）、`getTileIcon()`、`isBorderOpaque()`（`color != null`）。

## 2. Java 用户如何使用
用户用纯色或小图标铺出 matte 边框：

```java
MatteBorder colorBorder = new MatteBorder(1, 2, 3, 4, Color.RED);
Insets in = colorBorder.getBorderInsets();   // (1,2,3,4)
Icon tile = new ImageIcon("tile.png");
MatteBorder iconBorder = new MatteBorder(tile); // 边距 = (h,w,h,w)
```

典型组合链路：`new MatteBorder(top, left, bottom, right, color)` → `getMatteColor()`/`getTileIcon()` → `getBorderInsets()` → `isBorderOpaque()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static MatteBorder createMatteBorder(int top, int left, int bottom, int right, Color color) { return new MatteBorder(top, left, bottom, right, color); }` 与 `createMatteBorder(..., Icon tileIcon)`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_MatteBorder.java`：`return new MatteBorder(1, 2, 3, 4, Color.RED);`。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_plaf_BorderUIResource_MatteBorderUIResource.java` 使用 `MatteBorderUIResource` 构造。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `EmptyBorder`、非 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖——纯色构造边距 round-trip、`getMatteColor`、无图标、opaque；`Insets`+颜色构造；图标构造按图标宽高推导边距 `(5,7,5,7)`、`getTileIcon` round-trip、无颜色、不透明；显式边距+图标构造保留边距；颜色与图标两种 `paintBorder` smoke。

## 5. 深化追加（第二轮）
- 序列化 round-trip（color 变体）：insets/matteColor 保持、tileIcon 为 null。
- BorderFactory 数据属性：`createMatteBorder(int,int,int,int,Color)` 与 `(…,Icon)`（只测数据属性；注意 `createMatteBorder(Icon)` 单参重载不存在，icon insets 推导走 `new MatteBorder(Icon)` 构造器）。
