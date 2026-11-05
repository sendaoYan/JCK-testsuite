# org.w3c.dom.ranges.RangeException — 设计案

## 1. API 功能
org.w3c.dom.ranges.RangeException 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；Range.selectNode 触发 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Range）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 RangeExceptionTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量：BAD_BOUNDARYPOINTS_ERR=1、INVALID_NODE_TYPE_ERR=2；code 为 public 字段。
- 构造链：code+message 保留、null message 保留、RuntimeException 子类。
- 真实触发点：`Range.selectNode(未挂载节点)` → INVALID_NODE_TYPE_ERR(2)；异常后同一 Range 对象仍可用（状态未被破坏）。
