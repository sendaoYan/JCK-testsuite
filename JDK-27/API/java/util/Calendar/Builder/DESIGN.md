# java.util.Calendar.Builder 测试设计案（JDK-27）

> 覆盖目标：完整契约： setDate/setTime/setWeekDate/instant 各通道、lenient/strict 切换、ISO 8601 周定义、build() 可复用性。

## 1. API 功能

- 字段通道：setDate(y,m,d)/setTimeOfDay(h,m,s[,ms])/set(field,value)/setFields(int...)：fluent
- 基准：setInstant(long millis)；周通道：setWeekDate(y,w,dow)、setWeekDefinition(fdw,minDays)
- 模式：setLenient(true/false)、setLocale、setTimeZone；build() 产出 Calendar

## 2. 使用方式

new Calendar.Builder().setDate(2021, 1, 31).setLenient(false).build()；非法组合 → IllegalStateException/IllegalArgumentException。

## 3. 参考来源

- Oracle javadoc `java/util/Calendar.Builder`（builder 与 Calendar.getInstance 对照）
- JDK 源码 Calendar.Builder.build() 状态校验

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | setDate+setTimeOfDay 精确字段 | testBuilder |
| P0 | lenient month=13 折叠 vs strict 抛 IAE | testBuilderContracts |
| P0 | setWeekDate/setWeekDefinition ISO 周（周一起、4 天） | testBuilderContracts |
| P1 | build() 可再次调用产出新实例 | testBuilderContracts |
| P0 | 契约深化：lenient 归一化/严格模式/iso8601/weekDate/复用 | testBuilderContracts |
