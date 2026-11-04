# java.util.IntSummaryStatistics 测试设计案（JDK-8） 测试设计案

> 覆盖目标：完整契约：空状态字段精确值、流式累积、combine 合并、无参构造（JDK-22 才有 4 参构造，8 侧用反射证明缺失）。

## 1. API 功能

- 无参构造器（空状态：count=0、sum=0、min=Integer.MAX_VALUE、max=MIN_VALUE）
- 实例方法：accept/count/min/max/sum/average/getCount/getSum/getMin/getMax/getAverage/toString；combine

## 2. 使用方式

`new IntSummaryStatistics()` → `accept(...)` 序列 → 各 getter 精确断言；`combine` 合并两个实例等价于顺序 accept 全部元素。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/IntSummaryStatistics`（stream Collectors.summarizingXxx 的累加器）
- JDK 8 源码 IntSummaryStatistics.java（空状态 min/max 初始化值）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 空实例各 getter 精确（min/max 极值方向） | testStats |
| P0 | accept 序列累积 + average 除法精确 | testStats |
| P0 | combine(a,b) == 顺序累积 a 后 b | testCombine |
| P1 | JDK-22 4 参构造在本版缺失（反射 probe） | testSummaryContracts |
| P0 | 契约深化：空态边界/平均值精确/4 参构造(版本差异) | testSummaryContracts |
