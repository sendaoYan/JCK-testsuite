# javax.swing.Spring 设计案

## 1. API 功能
`Spring` 抽象类描述可伸缩尺寸。方法分组：
- 取值（抽象）：`getMinimumValue/getPreferredValue/getMaximumValue/getValue/setValue`。
- 工厂：`constant`（单值/范围）。
- 运算：`minus/sum/max/scale/width/height`。

## 2. Java 用户如何使用
布局管理器用 Spring 表达约束：
```java
Spring size = Spring.constant(50);
Spring total = Spring.sum(size, Spring.constant(10));
```
典型链路：`Spring.constant` → `getValue` → `sum/max/scale`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Spring.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Spring.java`）。
- `SpringLayout` 大量使用 `Spring.constant/sum/max/width/height`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖单值/范围 `constant`、`setValue`、`sum/minus/max/scale`、`width/height` 非空。
