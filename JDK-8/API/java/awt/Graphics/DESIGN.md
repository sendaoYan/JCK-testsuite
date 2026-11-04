# java.awtGraphics 黑盒行为测试设计

## 1. API 功能
图形上下文（经 BufferedImage.createGraphics() headless 实测）：drawLine 精确像素、drawRect 1px 边框内不染、fillRect 右下排他、clearRect 用背景色、copyArea 块复制、setClip/clipRect 交集/getClipBounds/setClip(null)、hitClip、translate 平移原点、字体往返、create() 状态复制且独立、XOR 两次同画复位、setPaintMode 覆盖恢复、dispose 两次安全。

## 2. Java 用户如何使用
```java
Graphics g = bi.createGraphics(); g.setXORMode(green); g.drawLine(..); g.drawLine(..);
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Graphics.java/SunGraphics2D.java：像素语义两端 JDK 一致（P5 探针锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：逐像素断言（端点/邻点）；fillRect [x,x+w) 排他；clearRect 用背景；copyArea；clip 交集与清空；hitClip 内外；translate；create() 独立性；XOR 双画复位；dispose×2。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
