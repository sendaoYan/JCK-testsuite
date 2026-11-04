# java.awtLayoutManager 黑盒行为测试设计

## 1. API 功能
布局接口：addLayoutComponent(String)/removeLayoutComponent/preferredLayoutSize/minimumLayoutSize/layoutContainer 五方法；匿名实现全回调验证；Container.getPreferredSize/getMinimumSize 委托布局；doLayout 触发 layoutContainer；FlowLayout 是实现。

## 2. Java 用户如何使用
```java
c.setLayout(lm); c.getPreferredSize() // 委托 lm
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 LayoutManager.java/Container.java 委托链。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：五方法回调；容器委托验证；doLayout 触发；FlowLayout instanceof。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
