# java.awtRectangle 黑盒行为测试设计

## 1. API 功能
矩形几何：add(Point) 就地扩/intersects/contains 双精度边界排他/intersection 空结果负尺寸/union 包围盒且吞掉空矩形/grow 负向收缩/outcode 掩码/reshape 别名/equals 跨 Rectangle2D.Double/Serializable(sUID=-4345857070255674764L)。

## 2. Java 用户如何使用
```java
r.add(new Point(-1,-1)); r.union(empty) -> 原包围盒新实例
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Rectangle.java：intersection 交空为 (0,0,0,0)；union 忽略空矩形（观察）；contains 右下边界排他。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：add(Point) 就地扩；贴合矩形交为零宽空矩形；相离为负尺寸；union 空矩形被忽略（观察）；contains/intersects 双精度排他；grow 负收缩；outcode 掩码；setBounds(Rectangle) 拷贝；reshape 别名；序列化；equals 跨实现。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
