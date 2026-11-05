# jdk.jfr.consumer.RecordingStream 设计案

## 1. API 功能
实时录制流（AutoCloseable）：构造/enable/setReuse/setOrdered/onEvent/onFlush。

## 2. Java 用户如何使用
new RecordingStream() 配置回调。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordingStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 构造 + 配置回调不抛异常。

## 5. 深化（本轮）
断言规模：基线 → 25。
### 行为闭环
- startAsync 流式闭环：enable 事件类型 + 注册 onEvent 收集 → 计数 == 3（与已知 commit 数相等）→ stop() == true → awaitTermination。
- enable/disable 返回 EventSettings；setSettings 生效。
- try-with-resources 全程。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 二次 `stop()` | IllegalStateException | （断言类型，"second stop should throw"） |
### 8/27 差异
RecordingStream 为 27 独有（8 侧无 jdk.jfr.consumer 流式 API）。
