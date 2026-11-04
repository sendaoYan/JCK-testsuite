# java.awtBasicStroke 黑盒行为测试设计

## 1. API 功能
线型：默认 CAP_SQUARE/JOIN_MITER/width 1/miter 10、全参构造读回、getDashArray 双向防御拷贝、IAE 消息精确（negative width/illegal end cap value/illegal line join value/miter limit < 1/dash lengths all zero）、dash phase 超周期被接受（规范偏离，观察）、createStrokedShape 精确包围盒、非 Serializable。

## 2. Java 用户如何使用
```java
new BasicStroke(2f, CAP_ROUND, JOIN_BEVEL).createStrokedShape(line)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 BasicStroke.java：dash phase 仅负数抛 IAE（javadoc 说须 < 周期，实现未校验——偏离锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：默认 CAP_SQUARE；全参读回；防御拷贝；6 条 IAE 消息逐字；phase 超周期接受（偏离注释）；createStrokedShape 包围盒 (9,9,52,52)/(8,54)；非 Serializable。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
