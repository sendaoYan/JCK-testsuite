# java.awtLayoutManager2 黑盒行为测试设计

## 1. API 功能
约束布局接口：extends LayoutManager，加 addLayoutComponent(Component,Object)/maximumLayoutSize/getLayoutAlignmentX/getLayoutAlignmentY/invalidateLayout；Container.add(Component,Object) 路由双参 addLayoutComponent；Container.getAlignmentX/Y 委托 LM2 对应方法（观察）；BorderLayout/GridBagLayout/CardLayout 是 LM2、FlowLayout 不是。

## 2. Java 用户如何使用
```java
lm2.getLayoutAlignmentX(c); c.getAlignmentX() // 委托
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 LayoutManager2.java/Container.java getAlignment 委托。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口层级；五新方法回调；约束 add 路由；容器 alignment 委托 0.25/0.75；四内建类分类。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
