# java.awt.SystemColor 黑盒行为测试设计

## 1. API 功能
- 常量索引：`DESKTOP=0` … `INFO_TEXT=25`、`NUM_COLORS=26`（系统颜色表长度）。
- 预定义实例：`desktop/window/windowText/control/menu/info/infoText` 等 26 个 `public static final SystemColor` 字段，均继承 `java.awt.Color`。
- 查询（继承自 Color + 覆写）：`getRGB()`、`getRed/getGreen/getBlue/getAlpha`、`toString()`（返回 `类名[i=索引]`）。

## 2. Java 用户如何使用
取系统主题色做 UI 配色，无需构造（构造器 private，只能取静态单例）：
```java
Color c = SystemColor.window;
int rgb = c.getRGB();          // 0xAARRGGBB
int blue = c.getBlue();        // 0..255
```
组合链路：`SystemColor.window` → `getRGB` → 拆通道 → `getAlpha` 校验不透明。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/SystemColor.java`：`updateSystemColors()` 在非 headless 下用 `Toolkit.loadSystemColors` 填充，headless 下保持默认 0（黑）；`toString` 返回 `getClass().getName() + "[i=" + index + "]"`。
- `src/java.desktop/share/classes/javax/swing/`、`com/sun/java/swing/plaf/` 使用 `SystemColor.*` 取原生配色做默认 L&F。
- `test/jdk/java/awt/SystemColor/`、`test/jdk/javax/swing/plaf/` 断言系统色不透明、RGB 通道在 [0,255]。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 索引常量精确值：`DESKTOP==0`、`WINDOW==7`、`INFO==24`、`INFO_TEXT==25`、`NUM_COLORS==26`。
- 预定义实例 `instanceof Color`、非空。
- `getRGB` 不透明（alpha==0xFF）、`getAlpha()==255`、三通道在 [0,255]。
- `getRGB == (alpha,red,green,blue) 组合`；进程内重复读取一致。
- `toString` 精确 `java.awt.SystemColor[i=7]`、`desktop` 为 `i=0`。
跨主题/跨平台健壮：不断言具体主题 RGB 值，只断结构/范围/不透明/确定性。
