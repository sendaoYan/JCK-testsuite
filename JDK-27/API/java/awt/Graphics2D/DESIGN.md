# java.awt.Graphics2D 黑盒行为测试设计

## 1. API 功能
抽象类，继承 `java.awt.Graphics`，是 2D 绘图上下文。实例通过 `BufferedImage.createGraphics()` / `Component.getGraphics()` / `GraphicsEnvironment.createGraphics(BufferedImage)` 获取，不直接 new。
- 绘制：`draw(Shape)`、`fill(Shape)`、`drawString(String,int,int)/(float,float)`、`drawGlyphVector`、`drawImage(BufferedImage,AffineTransform,ImageObserver)`、`drawImage(BufferedImage,BufferedImageOp,int,int)`、`drawRenderedImage`、`drawRenderableImage`、`draw3DRect`、`fill3DRect`。
- 状态：`setPaint/getPaint`、`setComposite/getComposite`、`setStroke/getStroke`、`setBackground/getBackground`。
- 渲染提示：`setRenderingHint/getRenderingHint`、`setRenderingHints/addRenderingHints/getRenderingHints`。
- 变换：`translate(int)/(double)`、`rotate(double)/(double,double,double)`、`scale`、`shear`、`transform(AffineTransform)`、`setTransform/getTransform`。
- 裁剪/命中：`clip(Shape)`（继承 `getClipBounds`）、`hit(Rectangle,Shape,boolean)`。
- 查询：`getDeviceConfiguration()`、`getFontRenderContext()`。

## 2. Java 用户如何使用
```java
BufferedImage bi = new BufferedImage(64, 64, TYPE_INT_ARGB);
Graphics2D g = bi.createGraphics();
g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
g.setPaint(Color.RED);
g.fill(new Rectangle2D.Float(10, 10, 20, 20));
g.setStroke(new BasicStroke(3.0f));
g.draw(new Line2D.Double(0, 0, 40, 40));
g.setTransform(AffineTransform.getScaleInstance(2, 2));
g.drawString("Hi", 5, 20);
g.dispose();
int px = bi.getRGB(15, 15);
```
组合链路：`createGraphics` → `setPaint/setStroke/setRenderingHint` → `fill/draw/drawString` → `getRGB` 断言像素。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/Graphics2D/DrawString/DrawRotatedString.java`：`BufferedImage.createGraphics()` + `setRenderingHint(KEY_ANTIALIASING,...)` + `drawString` + `getRGB` 验证文字位置（与本用例同款内存图像路径）。
- `test/jdk/java/awt/Graphics2D/TransformSetGet/TransformSetGet.java`：`gc.createCompatibleVolatileImage(...).createGraphics()` + `setTransform(getTransform())` 验证变换 set/get 一致性。
- `test/jdk/java/awt/dnd/ImageDecoratedDnDInOut/MyCursor.java`：`createGraphics()` + `drawImage` 绘制自定义光标位图。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Graphics2D.java`：`setRenderingHints` 委派 `SunGraphics2D`，`draw/fill` 为 abstract。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，BufferedImage TYPE_INT_ARGB + getRGB 精确断言，headless-safe）：
- 初始状态：transform 恒等、clip 为 null、background 黑、paint 白、composite/stroke 非 null。
- setPaint/getPaint 往返；fill(Rectangle2D) 内部像素红、外部透明；draw(Line2D)+BasicStroke(3) 线像素着色。
- AlphaComposite.Clear 清空已填充区域（确定性擦除）。
- setRenderingHint/getRenderingHint、addRenderingHints/setRenderingHints/getRenderingHints 往返。
- translate/rotate(90° 映射 (1,0)->(0,1))/scale/shear/transform/setTransform 的变换数学（1e-6 容差）。
- clip(Rectangle2D) 后 getClipBounds 精确、fill 只在裁剪内生效。
- hit(Rectangle,Shape,false) 相交/不相交。
- drawString/drawGlyphVector 产生非黑像素（>0）。
- drawImage 三形态 + drawRenderedImage 像素红；fill3DRect/draw3DRect 着色。
断言 87 项，白盒 30 + 黑盒 57。
