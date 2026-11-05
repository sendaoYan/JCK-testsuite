# org.w3c.dom.ranges.RangeException — 设计案

## 1. API 功能
org.w3c.dom.ranges.RangeException 类。常量：BAD_BOUNDARYPOINTS_ERR, INVALID_NODE_TYPE_ERR（共 2 个）。

## 2. Java 用户如何使用
常量断言 + new(short,String) → code/getMessage

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ranges.RangeException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 RangeExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`RangeExceptionTest.testBlackBox()`）覆盖：常量 + code 字段 + 构造。

## 深化第 2 轮（契约级，2026-11-05）
- 常量：BAD_BOUNDARYPOINTS_ERR=1、INVALID_NODE_TYPE_ERR=2；code 为 public 字段。
- 构造链：code+message 保留、null message 保留、RuntimeException 子类。
- 真实触发点：`Range.selectNode(未挂载节点)` → INVALID_NODE_TYPE_ERR(2)；异常后同一 Range 对象仍可用（状态未被破坏）。
