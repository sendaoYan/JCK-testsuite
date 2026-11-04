# javax.swing.BoundedRangeModel — DESIGN

## 1. API 功能
接口（`javax.swing.BoundedRangeModel`），定义 Slider/ProgressBar/ScrollBar 共用的有界范围模型四元组 + 调整标志：
- 状态查询：`getValue/getExtent/getMinimum/getMaximum/getValueIsAdjusting`
- 单属性设置（实现负责钳制）：`setValue/setExtent/setMinimum/setMaximum/setValueIsAdjusting`
- 一次性联合设置：`setRangeProperties(value, extent, min, max, adjusting)`（至多触发一个 ChangeEvent）
- 监听：`addChangeListener/removeChangeListener`
- 参考实现：`javax.swing.DefaultBoundedRangeModel`（默认 value=0, extent=0, min=0, max=100, adjusting=false）

## 2. Java 用户如何使用
- 构造：`new DefaultBoundedRangeModel()` 用默认值；`new DefaultBoundedRangeModel(value, extent, min, max)` 要求 `min <= value <= value+extent <= max`，否则 IAE。
- 典型链路（进度条语义）：`model.setRangeProperties(40, 0, 0, total, false)` → `model.getValue()` → 拖动期间 `setValueIsAdjusting(true)` + 多次 `setValue(n)`（自动钳制到 `[min, max-extent]`）→ `setExtent(k)` 表示可见范围 → `addChangeListener(e -> ...)` 刷新 UI。
- 钳制语义（读 jdk25u `DefaultBoundedRangeModel.java` 确认）：`setValue` 钳到 `min..max-extent`；`setExtent` 钳到 `>=0` 且 `value+extent<=max`；`setMinimum` 会抬升 value（必要时抬 max）；`setMaximum` 会压低 value/extent；`setRangeProperties` 强制满足约束：`value>max` 时 **max 长大到 value**，`value<min` 时 min 缩小，`min>max` 时 min=max，extent 钳到 `max-value`，任何字段变化才发一个事件。

## 3. 开源用法调研
WebSearch 在本环境不可靠（返回无真实链接的生成内容），按约定改用可验证的 JDK 源码/自带测试并标注来源：
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JSlider.java`：JSlider 构造即 `new DefaultBoundedRangeModel(value, extent, min, max)`，拖动滑块时经 `BoundedRangeModel.setValue` + `setValueIsAdjusting` 驱动。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JProgressBar.java` / `JScrollBar.java`：以 `BoundedRangeModel` 为可替换模型，`setValue/setMaximum` 全部委托给模型并依赖其钳制。
- `~/git/jdk25u-dev/test/jdk/javax/swing/RangeTest.java`：JDK 自测直接构造 `DefaultBoundedRangeModel` 断言 value/extent/min/max 钳制关系。

## 4. 测试用例设计（黑盒部分，`BoundedRangeModelTest.testBlackBox`）
以接口类型 `BoundedRangeModel` 持有 `DefaultBoundedRangeModel` 实例驱动：
1. 默认构造五属性初值（0/0/0/100/false）。
2. 4 参构造合法往返 + 三种非法（value<min、max<min、value+extent>max）→ IAE。
3. setValue 上下钳制与 `max-extent` 钳制；setExtent 负数/超界钳制与正常往返。
4. setMinimum 抬 value / 不降 value；setMaximum 压 value、extent。
5. setRangeProperties：五属性一次读回；value>max → max 长大；value<min → min 缩小；min>max → min=max；extent 钳制；adjusting 位。
6. setValueIsAdjusting 往返。
7. ChangeListener 记录链：setValue 1 事件（source==model）、同值 0 事件、联合修改恰 1 事件、重复 0 事件、仅 adjusting 翻转 1 事件、remove 后 0 事件。
断言合计 45 条（PASS 行 N）。
