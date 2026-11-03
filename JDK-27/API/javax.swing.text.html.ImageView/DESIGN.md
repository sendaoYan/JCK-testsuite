# javax.swing.text.html.ImageView — DESIGN

## 1. API 功能
HTML 图片视图（`public class ImageView extends View`），渲染 `<img>` 元素，支持同步/异步图片加载。关键方法：
- `getLoadingImageIcon()` / `getNoImageIcon()`：返回加载中/加载失败的默认 Icon（非 null）。
- `setLoadsSynchronously(boolean)`：控制同步加载。
- `getAlignment(int axis)`：Y 轴返回 1（底部对齐），X 轴默认 0.5。
- `getPreferredSpan/getMinimumSpan/getMaximumSpan`：图片尺寸，min==max==pref。
- `setSize/paint`：按图像实际尺寸绘制。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<img>` 创建。图片加载失败时显示 `getNoImageIcon()` 的默认占位图标。黑盒测试用带 `width/height` 与缺失 src 的 `<img>`，物化视图树后取 live ImageView 断言尺寸与图标。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ImageView.java`：`getAlignment(Y_AXIS)` 返回 1、`getNoImageIcon` 返回静态默认图标。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JEditorPane/8195095/ImageViewTest.java`：ImageView 尺寸回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/synth/Test6933784.java`：图片视图相关回归。

## 4. 测试用例设计（黑盒部分，`ImageViewTest.testBlackBox`）
1. 从视图树取 live ImageView；`getElement()` 往返。
2. `getAlignment(Y_AXIS) == 1`。
3. X/Y 轴 `min == max == pref`。
4. `getNoImageIcon()`、`getLoadingImageIcon()` 均非 null 且 `instanceof Icon`。
5. 非法 axis 查询 → `IllegalArgumentException`。
