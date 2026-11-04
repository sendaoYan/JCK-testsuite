# java.awt.DisplayMode 黑盒行为测试设计

## 1. API 功能
- 常量：`BIT_DEPTH_MULTI=-1`、`REFRESH_RATE_UNKNOWN=0`。
- 构造：`DisplayMode(width,height,bitDepth,refreshRate)`。
- 查询：`getWidth/getHeight/getBitDepth/getRefreshRate`。
- 值对象：`equals(DisplayMode)`/`equals(Object)`/`hashCode()`/`toString()`。

## 2. Java 用户如何使用
描述显示模式，供 `GraphicsDevice.getDisplayModes()` 返回、`setDisplayMode()` 设置。
```java
DisplayMode dm = new DisplayMode(1920, 1080, 32, 60);
int w = dm.getWidth(); boolean same = dm.equals(other);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/DisplayMode.java`：`hashCode = width+height+bitDepth*7+refreshRate*13`；`toString` 在 bitDepth>0 用 `Xbpp`、refreshRate>0 用 `YHz`，否则 `[Multi depth]`/`[Unknown refresh rate]`。
- `test/jdk/java/awt/DisplayMode/` 与 `GraphicsDevice/` 用 DisplayMode 断言显示模式属性。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量精确值；四属性构造回读。
- `equals` 对称/相等/不等/null/异类；相等 hashCode 相等。
- `hashCode` 公式、`toString` 精确格式（普通与 multi/unknown 两路）。
纯数值/字符串，headless-safe。
