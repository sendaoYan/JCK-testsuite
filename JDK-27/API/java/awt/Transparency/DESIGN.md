# java.awt.Transparency 黑盒行为测试设计

## 1. API 功能
- 接口 + 常量：`OPAQUE=1`、`BITMASK=2`、`TRANSLUCENT=3`。
- 方法：`int getTransparency()`。

## 2. Java 用户如何使用
实现此接口表示图像/颜色模型的透明度类型（BufferedImage、ColorModel 等）：
```java
int t = image.getTransparency(); // OPAQUE / BITMASK / TRANSLUCENT
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Transparency.java`（接口 + 常量）。
- `src/java.desktop/share/classes/java/awt/image/BufferedImage.java`、`ColorModel.java`、`java/awt/Color.java` 等实现 `Transparency`。
- `test/jdk/java/awt/image/` 断言图像透明度类型常量。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 三常量精确值且互异。
- 接口白盒（getTransparency 返回 int）。
- 匿名实现返回 OPAQUE/TRANSLUCENT 回读。
headless-safe。
