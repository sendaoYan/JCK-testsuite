# java.awt.Graphics 黑盒行为测试设计

## 1. API 功能
- 抽象基类（实际经 `BufferedImage.createGraphics()` 得到 `Graphics2D` 子类）。
- 状态：`getColor/setColor`、`getFont/setFont`、`getFontMetrics`、`getClipBounds/getClip/setClip/clipRect/getClipRect/hitClip`、`setPaintMode/setXORMode`。
- 几何：`translate`、`create`、`copyArea`、`drawLine`、`fillRect/drawRect/clearRect`、`drawRoundRect/fillRoundRect`、`draw3DRect/fill3DRect`、`drawOval/fillOval`、`drawArc/fillArc`、`drawPolyline/drawPolygon/fillPolygon`。
- 文本：`drawString/drawChars/drawBytes`。
- 图像：`drawImage`（六重载）。
- `dispose`、`toString`、`finalize`。

## 2. Java 用户如何使用
所有自定义 `Component.paint(Graphics g)` 的入口：设色/设字体 → 画形状/文本 → 命中/裁剪。
```java
public void paint(Graphics g) {
    g.setColor(Color.RED);
    g.fillRect(0, 0, w, h);
    g.setFont(font);
    g.drawString("Hi", x, y);
}
```
组合链路：`createGraphics` → `setColor/setFont` → `setClip` → `fillRect` → 像素校验 → `translate` → 再画 → `dispose`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Graphics.java`：抽象方法签名与默认实现（`create(x,y,w,h)` 返回带平移的子 Graphics）；`hitClip`/`getClipBounds(Rectangle)` 便捷方法。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Graphics/SmallPrimitives.java`（`new Polygon` + 各形状 draw）、`test/jdk/java/awt/Graphics/PolygonFillTest.java`（`fillPolygon`）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `setColor/getColor`、`setFont/getFont`、`getFontMetrics`。
- 裁剪：`setClip(int,int,int,int)` 后 `getClipBounds/getClipRect/getClip` 精确；`hitClip` 内外；`getClipBounds(Rectangle)` 填充并返回参数；`clipRect` 交集。
- `translate` 平移后续绘制（像素校验偏移）；`create()` 继承状态且子改不影响父；`create(x,y,w,h)`。
- 像素精确：`fillRect` 填充内部像素/不动外部；`clearRect`（透明背景）清空；`drawRect` 只描边不填充；`drawLine` 沿线像素。
- 形状/文本/图像原语可调用不抛异常。
- `setXORMode`→`setPaintMode` 保留颜色。
- `dispose` 可调用。
断言 72 项，像素断言为固定整数（TYPE_INT_ARGB 全内存操作，headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：逐像素断言（端点/邻点）；fillRect [x,x+w) 排他；clearRect 用背景；copyArea；clip 交集与清空；hitClip 内外；translate；create() 独立性；XOR 双画复位；dispose×2。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
