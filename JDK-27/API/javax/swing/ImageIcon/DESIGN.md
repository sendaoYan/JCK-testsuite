# javax.swing.ImageIcon 设计案

## 1. API 功能
`ImageIcon` 是 `Icon` 的实现，包装一张 `Image`。方法分组：
- 构造：`ImageIcon(Image[, description])`、`ImageIcon(String|URL|byte[])`、`ImageIcon()`。
- 查询：`getImage/setImage`、`getIconWidth/getIconHeight`、`getImageLoadStatus`、`getDescription/setDescription`。
- 绘制：`paintIcon(Component, Graphics, x, y)`。
- 观察者：`setImageObserver/getImageObserver`。

## 2. Java 用户如何使用
用户加载图片并挂到按钮/标签：
```java
ImageIcon icon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
button.setIcon(icon);
```
典型链路：`new ImageIcon(image)` → `getIconWidth/getIconHeight` → `setDescription` → `paintIcon`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 等用 `ImageIcon` 提供图标（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/`）。
- JDK 测试 `test/jdk/javax/swing/ImageIcon/` 覆盖图像加载与绘制。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `Icon`+`Serializable`+`Accessible`。
- 行为（黑盒）：`testBlackBox()` 用 `BufferedImage` 覆盖 `getIconWidth/getIconHeight/getImage/getImageLoadStatus`、`setDescription/getDescription`、`paintIcon` smoke。
