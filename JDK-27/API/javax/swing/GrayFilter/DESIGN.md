# javax.swing.GrayFilter 设计案

## 1. API 功能
`GrayFilter` 是继承 `RGBImageFilter` 的图像滤镜，把彩色图转为灰度/明暗图。方法分组：
- 构造：`GrayFilter(boolean brightness, int percent)`。
- 过滤：`filterRGB(int x, int y, int rgb)`。
- 工厂：`static createDisabledImage(Image)`（生成禁用图标）。

## 2. Java 用户如何使用
用户用它生成禁用状态的灰度图标：
```java
Image disabled = GrayFilter.createDisabledImage(originalIconImage);
```
典型链路：`new GrayFilter(brightness, percent)` → `filterRGB` → `createDisabledImage`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 的 `getDisabledIcon` 内部使用 `GrayFilter.createDisabledImage`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`）。
- JDK 源码 `javax/swing/ImageIcon.java` 不直接使用，但禁用图标统一走 `GrayFilter`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `RGBImageFilter`、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖 `filterRGB` 的变暗（percent=100→opaque black）、变亮（brightness→opaque white）、灰度（percent=0→r==g==b 且 alpha 保留）、`createDisabledImage` 非空。
