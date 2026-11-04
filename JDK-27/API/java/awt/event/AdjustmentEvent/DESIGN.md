# java.awt.event.AdjustmentEvent 行为测试设计

## 1. API 功能
可调组件（滚动条）值变化事件，带 adjustable 源、value、adjustmentType（UNIT/BLOCK/TRACK 增量）、isAdjusting 标志，常量 ADJUSTMENT_VALUE_CHANGED=601。

## 2. Java 用户如何使用
`new AdjustmentEvent(scrollbar, ADJUSTMENT_VALUE_CHANGED, UNIT_INCREMENT, v)` 表示滚动；AdjustmentListener 读 getValue/getAdjustmentType/getValueIsAdjusting。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/AdjustmentEvent.java`；`Scrollbar.java` 触发路径。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量与 4/5 参构造器。黑盒：构造→回读各 getter；5 参 isAdjusting；6 个常量精确值；paramString 含 id/adjType/value/isAdjusting。
对应 `AdjustmentEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 4/5 参构造器全走查；越界 adjustmentType（99）与负 value 无校验、原样回读。
- 消费语义：ADJUSTMENT_VALUE_CHANGED 属语义 id，构造即 consumed；consume() 保持。
- paramString 精确片段：adjType=/value=/isAdjusting=。
- 序列化：id/value/adjustmentType/isAdjusting 保留（DummyAdjustable 需实现 Serializable）。
