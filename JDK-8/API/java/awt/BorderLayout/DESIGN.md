# java.awtBorderLayout 黑盒行为测试设计

## 1. API 功能
边框布局：五区域 NORTH/SOUTH/EAST/WEST/CENTER，100x100 精确 N=(0,0,100,20) S=(0,70,100,30) E=(95,20,5,50) W=(0,20,15,50) C=(15,20,80,50)；hgap=2/vgap=3 时 C=(17,23,76,44)；N/S 跨全宽钳制；prefdim 聚合；未挂载实例报 (0,0)（观察）；null 约束=CENTER；bogus IAE 'cannot add to layout: unknown constraint: bogus'；removeLayoutComponent(null) NPE；alignment 0.5。

## 2. Java 用户如何使用
```java
p.add(north, BorderLayout.NORTH); p.setSize(100,100); p.doLayout();
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 BorderLayout.java：内部分区字段由 addLayoutComponent 填充（故未挂载实例看不到孩子——锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：五区域像素级；hgap/vgap 变体；N/S 钳制；prefdim (30,40)/(50,90)；未挂载 (0,0)；null=CENTER；IAE 消息逐字；null 移除 NPE；alignment。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
