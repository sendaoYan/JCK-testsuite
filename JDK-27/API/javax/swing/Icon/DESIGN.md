# javax.swing.Icon 设计案

## 1. API 功能
`Icon` 是可绘制小图标接口。方法分组：
- 绘制：`paintIcon(Component, Graphics, int, int)`。
- 尺寸：`getIconWidth()`、`getIconHeight()`。

## 2. Java 用户如何使用
用户实现它或使用 `ImageIcon`，挂到按钮/标签上：
```java
button.setIcon(new Icon() {
    public void paintIcon(Component c, Graphics g, int x, int y) { /* draw */ }
    public int getIconWidth() { return 16; }
    public int getIconHeight() { return 16; }
});
```
典型链路：`getIconWidth/getIconHeight` → `paintIcon`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ImageIcon.java` 实现 `Icon`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ImageIcon.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicIconFactory.java` 等返回自定义 `Icon`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、无父接口。
- 行为（黑盒）：`testBlackBox()` 用匿名实现覆盖 `getIconWidth/getIconHeight/paintIcon` 三个方法。
