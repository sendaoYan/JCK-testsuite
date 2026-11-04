# java.awtColor 黑盒行为测试设计

## 1. API 功能
颜色值类：13 个预定义常量精确 RGB+不透明、float 构造 0.5f→128（+0.5 取整）、packed 构造 hasalpha=false→0xFFABCDEF、brighter/darker 精确（黑→(3,3,3)）、decode 变体+NumberFormatException、HSB 往返、getColor 带属性、equals 含 alpha、hashCode==getRGB、ColorSpace 构造、序列化保 alpha、getTransparency。

## 2. Java 用户如何使用
```java
Color.decode("0xAABBCC"); Color.getHSBColor(h,s,b); c.brighter().darker().equals(c) 近似
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Color.java：FACTOR=0.7；float 构造 (int)(f*255+0.5)；hashCode==getRGB。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：常量表精确值；float 0.5f→128；packed 0x89ABCDEF,false→getRGB 0xFFABCDEF；brighter/darker 精确；范围不变量；HSB 往返；decode+NFE；IAE 矩阵(300/-1/1.5f/-0.1f)；属性系统色；equals 含 alpha；hashCode==getRGB；序列化保 alpha；toString 精确。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
