# java.awtGraphics2D 黑盒行为测试设计

## 1. API 功能
2D 上下文：setStroke/getStroke、setComposite/getComposite、setPaint/getPaint 恒等往返、setRenderingHint/addRenderingHints 合并/setRenderingHints(Map) 复位为默认再套用（观察）、setBackground/getBackground、draw(Shape) 仅边框、fill(Shape) 内部、rotate 90° 精确映射、scale 2x 覆盖 [4,10)、shear/transform/setTransform identity、clip(Shape) 交集、hit(Rectangle,Shape,onStroke)、getFontRenderContext、drawString 可见像素。

## 2. Java 用户如何使用
```java
g2.scale(2,2); g2.fillRect(2,2,3,3) // 覆盖 [4,10)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 SunGraphics2D.java：hints 复位默认行为；JDK-27 同（探针双端锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：三 setter 恒等；hints 三接口语义；背景往返；draw/fill Shape 像素；rotate 映射 (12,10)→(10,12)；scale 边界；clip 交集像素；hit 双分支；FRC 非空；drawString 出像素。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
