# javax.management.InvalidApplicationException 契约测试设计（新建 + 深化 2026-11）

## 1. API 功能
- 查询子表达式应用于错误 MBean 类时抛出（JMX 内部异常）；仅 `(Object val)` 一个公开构造器；**无公开 getVal()**（val 为私有 serial 字段）。

## 2. Java 用户如何使用
用户代码通常不直接接触；Query 表达式实现内部使用。

## 3. 参考来源
- Oracle javadoc javax.management.InvalidApplicationException；javap 双侧对照。
- 实现权威：`src/java.management/share/classes/javax/management/InvalidApplicationException.java`。

## 4. 测试用例设计 + 易错规范细节
- 构造：`(Object)`；getMessage() 恒 null；**不在 JMException 层次**（直接 extends Exception，isInstance false）。
- serial 字段实证：ObjectStreamClass.getFields() 恰一个 `val`/Object。
- serialVersionUID = `-3048022274675537269L`（两侧同值）；多类型 val 序列化 round-trip。
