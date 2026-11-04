# java.awtShape 黑盒行为测试设计

## 1. API 功能
几何形状接口：getBounds/getBounds2D/contains×4/intersects×2/getPathIterator(+flatness)；Rectangle 与 Rectangle2D.Double 跨实现一致、Polygon 也是 Shape、等价形状 PathIterator 段数相同。

## 2. Java 用户如何使用
```java
Shape s = new Rectangle(0,0,10,10); s.getPathIterator(null)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Shape.java 接口；各实现 contains/intersects 语义一致（探针）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口方法存在；跨实现 contains/intersects 一致；Polygon contains；段数相同（矩形 6 段）。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
