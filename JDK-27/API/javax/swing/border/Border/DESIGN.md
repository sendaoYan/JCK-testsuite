# javax.swing.border.Border 设计案

## 1. API 功能
`Border` 是 Swing 边框的根接口，声明三个抽象方法：

- `paintBorder(Component, Graphics, int x, int y, int width, int height)`：绘制边框。
- `Insets getBorderInsets(Component)`：返回边框占用的边距。
- `boolean isBorderOpaque()`：边框是否不透明。

## 2. Java 用户如何使用
用户以匿名内部类或具体实现（`LineBorder`、`EmptyBorder` 等）实现该接口，交给 `JComponent.setBorder` 使用：

```java
Border b = new Border() {
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { /* 绘制 */ }
    public Insets getBorderInsets(Component c) { return new Insets(1, 2, 3, 4); }
    public boolean isBorderOpaque() { return true; }
};
component.setBorder(b);
```

典型组合链路：实现三个方法 → `setBorder` → 布局时 `getBorderInsets` 决定内边距 → 重绘时 `paintBorder` 绘制 → `isBorderOpaque` 决定是否需清背景。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/border/AbstractBorder.java`：`public abstract class AbstractBorder implements Border, Serializable`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/border/AbstractBorder.java`）。
- JDK 源码 `javax/swing/border/LineBorder.java`、`EmptyBorder.java`、`CompoundBorder.java` 等均 `implements`/继承该接口。
- JDK 源码 `javax/swing/JComponent.java`：`public void setBorder(Border border)` 与 `public Border getBorder()`（组件对外暴露 `Border` 类型）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验为 interface、无父类、无父接口，且恰有 `paintBorder`/`getBorderInsets`/`isBorderOpaque` 三个 public abstract 方法。
- 行为（黑盒）：`testBlackBox()` 用两个匿名实现验证——边距 round-trip（`new Insets(1,2,3,4)`）、`isBorderOpaque` 取值、`paintBorder` 被调用（用 `boolean[]` 捕获）；第二个实现验证相互独立（0 边距 + 透明）。
