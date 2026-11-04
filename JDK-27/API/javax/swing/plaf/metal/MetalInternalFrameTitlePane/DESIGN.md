# javax.swing.plaf.metal.MetalInternalFrameTitlePane — DESIGN

## 1. API 功能
`MetalInternalFrameTitlePane` 是 Metal 观感下 `JInternalFrame` 标题栏的实现（`extends BasicInternalFrameTitlePane`，非抽象）。它负责标题栏的绘制、按钮布局与窗口控制交互，是 `MetalInternalFrameUI` 的标题栏组件。

## 2. Java 用户如何使用
用户通过 `MetalInternalFrameUI` 获得标题栏；也可直接构造：`new MetalInternalFrameTitlePane(new JInternalFrame())`。它作为 `JComponent` 可参与布局，并继承标题栏绘制与关闭/最小化/最大化按钮处理。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalInternalFrameTitlePane.java:47`：`public class MetalInternalFrameTitlePane extends BasicInternalFrameTitlePane`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/MetalInternalFrameTitlePane/bug4221007.java:35-36`：`MetalInternalFrameTitlePane mitftp = new MetalInternalFrameTitlePane(new JInternalFrame());` 验证其为 public 可构造。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalInternalFrameUI.java:43`：`MetalInternalFrameUI`（标题栏的宿主 UI）。

## 4. 测试用例设计（黑盒部分，`MetalInternalFrameTitlePaneTest.testBlackBox`）
1. 用 `JInternalFrame` 构造标题栏并断言非空。
2. 标题栏为 `JComponent`，可设置 `paintable` 状态。
3. 设置大小后画到 `BufferedImage`（`paint`）无异常。
