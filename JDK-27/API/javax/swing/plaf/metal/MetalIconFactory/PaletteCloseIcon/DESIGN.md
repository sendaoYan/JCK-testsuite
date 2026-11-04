# javax.swing.plaf.metal.MetalIconFactory.PaletteCloseIcon — DESIGN

## 1. API 功能
`MetalIconFactory.PaletteCloseIcon` 是 Metal 观感调色板关闭按钮图标（`implements Icon, UIResource, Serializable`，`public` 静态嵌套类）。`paintIcon` 将传入组件强制转换为 `JButton` 并读取其 `ButtonModel` 以决定按下/悬停配色。

## 2. Java 用户如何使用
`MetalIconFactory.PaletteCloseIcon icon = new MetalIconFactory.PaletteCloseIcon(); icon.paintIcon(button, g, 0, 0);`。由于 `paintIcon` 依赖 `JButton`，绘制时必须传入 `JButton`（或其子类）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:667`：`public static class PaletteCloseIcon implements Icon, UIResource, Serializable`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:676`：`JButton parentButton = (JButton)c;`（强制转换，说明组件类型契约）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalIconFactory/bug4952462.java:53`：Ocean 主题下验证按钮类图标绘制（同族图标测试）。

## 4. 测试用例设计（黑盒部分，`PaletteCloseIconTest.testBlackBox`）
1. 构造实例并断言为 `Icon`、`UIResource`、`Serializable`。
2. 宽高非负（均为 7）。
3. 以 `JButton` 为组件 `paintIcon` 到 `BufferedImage` 无异常。
