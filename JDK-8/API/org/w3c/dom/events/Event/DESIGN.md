# org.w3c.dom.events.Event — 设计案

## 1. API 功能
org.w3c.dom.events.Event 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；(EventTarget) 派发链路 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Events 模块）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 EventTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
黑盒行为覆盖（真实派发链路验证）：
- 常量表：CAPTURING_PHASE=1、AT_TARGET=2、BUBBLING_PHASE=3。
- initEvent 设置 type/bubbles/cancelable 且可重复初始化覆盖；派发之外 target/currentTarget 为 null、phase 归 0/AT_TARGET。
- 捕获→目标→冒泡全序：capture 阶段 currentTarget=祖先、target=目标；目标节点 phase=AT_TARGET 且 target==currentTarget；bubbles=false 时祖先只看到捕获阶段；派发中 getTimeStamp()>0。
- stopImmediatePropagation/getDefaultPrevented 在 Level 2 接口缺席。
- 版本相关实现事实（双侧一致，与 DOM 规范相反）：Xerces `dispatchEvent` 返回值反转——无 preventDefault 返回 false、preventDefault 后返回 true；未初始化事件派发 → `EventException UNSPECIFIED_EVENT_TYPE_ERR(0)`。
