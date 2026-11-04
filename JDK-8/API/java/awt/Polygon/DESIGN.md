# java.awtPolygon 黑盒行为测试设计

## 1. API 功能
多边形：构造器拷贝数组、xpoints.length≥npoints 容量绑定、n>容量 IndexOutOfBoundsException、负 n NegativeArraySizeException、contains 边界语义、bounds 缓存+invalidate、translate/reset、getBounds2D、实现 Shape、Serializable。

## 2. Java 用户如何使用
```java
p.addPoint(1,2); p.contains(5,0) 边界判定；p.invalidate() 强制重算 bounds
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Polygon.java：偶奇规则 contains；bounds 缓存到 boundsLock；JDK 自带测试 test/jdk/java/awt/geom/Polygon 含。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：构造器数组拷贝；容量 20 点→32；越界/负数异常精确；contains 边界锁定((5,0)=true,(0,0)=true,(10,10)=false)；bounds 缓存；translate/reset；getBounds2D；序列化。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
