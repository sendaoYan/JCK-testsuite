# javax.swing.Painter 设计案

## 1. API 功能
`Painter<T>` 接口用于自定义绘制。方法分组：
- 绘制：`paint(Graphics2D, T, int, int)`。

## 2. Java 用户如何使用
用户实现 Painter 绘制到 `Graphics2D`：
```java
Painter<MyBean> painter = new Painter<MyBean>() {
    public void paint(Graphics2D g, MyBean obj, int w, int h) { g.fillRect(0, 0, w, h); }
};
```
典型链路：`new Painter(){...}` → `paint(graphics, object, width, height)`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Painter.java` 定义该接口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Painter.java`）。
- `javax.swing.plaf.nimbus` 大量使用 `Painter` 渲染外观。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 匿名实现绘制到 `BufferedImage` 的 `Graphics2D`，验证被调用。
