# javax.swing.SizeRequirements 设计案

## 1. API 功能
`SizeRequirements` 描述组件尺寸约束。方法分组：
- 字段：`minimum/preferred/maximum/alignment`。
- 组合：`getTiledSizeRequirements/getAlignedSizeRequirements`。
- 布局计算：`calculateTiledPositions/calculateAlignedPositions/adjustSizes`。

## 2. Java 用户如何使用
布局管理器用它计算组件尺寸与位置：
```java
SizeRequirements[] reqs = { new SizeRequirements(0, 50, 100, 0.5f) };
SizeRequirements total = SizeRequirements.getTiledSizeRequirements(reqs);
```
典型链路：构造 → `getTiledSizeRequirements` → `calculateTiledPositions`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BoxLayout.java`/`GroupLayout.java` 使用 `SizeRequirements`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BoxLayout.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖字段与构造、`toString`、平铺/对齐尺寸聚合、位置计算、尺寸调整（纯数学确定性断言）。
