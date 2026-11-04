# javax.swing.text.html.ImageView — DESIGN

## 1. API 功能
HTML 图片视图（`public class ImageView extends View`），渲染 `<img>`，支持同步/异步加载。关键方法：
- `getAltText()`/`getImageURL()`/`getImage()`：替代文本/地址/图像。
- `getLoadingImageIcon()`/`getNoImageIcon()`：加载中/失败占位图标（非 null）。
- `setLoadsSynchronously(boolean)`/`getLoadsSynchronously()`：默认异步。
- `getAlignment(int axis)`：(0.5, 1.0)。
- `getPreferredSpan/getMinimumSpan/getMaximumSpan`：图像或回退图标尺寸。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<img>` 创建。单元级测试可对 img 元素直接 `new ImageView(elem)`（Metal LaF）断言模型层契约：无 base 时相对 src → getImageURL() null、图标回退尺寸、对齐值。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/ImageView.java`：`getAlignment(Y_AXIS)` 返回 1、图标回退尺寸来自 NoImageIcon/LoadingImageIcon。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/`：图片视图相关回归。

## 4. 测试用例设计（黑盒，`ImageViewTest`：testShape + testModelLayer，38 断言）
1. testShape：superclass View、getAltText/getImageURL/getNoImageIcon/getLoadingImageIcon/getImage/setLoadsSynchronously/getLoadsSynchronously 方法存在。
2. 模型层（Metal LaF + 固定 HTML）：img 元素身份、ALT 往返、相对 src 无 base → getImageURL()==null、两图标非 null、loadsSynchronously 默认 false + set 往返。
3. 尺寸：preferred X/Y 取图标回退尺寸（prefX=59/prefY=53 侧向一致断言，JDK-8 与 JDK-27 相同）；alignment (0.5f, 1.0f)。
4. 负例：非法 axis → IAE（精确异常）。
