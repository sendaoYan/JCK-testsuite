# java.util.EmptyStackException 测试设计案（JDK-8）

> 覆盖目标：完整契约：Stack 空栈 pop/peek 触发、无参构造、null 消息特征。

## 1. API 功能

- 无参构造器（无消息构造通道）；继承 RuntimeException

## 2. 使用方式

new Stack() 后 pop()/peek()/peekAt → EmptyStackException；getMessage() 为 null。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/EmptyStackException`
- JDK 源码 Stack.empty 触发点

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 空栈 pop/peek 触发 | testThrownByStack |
| P0 | 异常类型继承 RuntimeException 且消息 null | testEmptyStackContracts |
| P0 | 契约深化：null 消息/Stack 触发矩阵/层次；唯一构造器/序列化 round-trip/空栈 search 不抛/栈轨迹可设 | testEmptyStackContracts、testStackTraceContract |
