# org.w3c.dom.events.Event — 设计案

## 1. API 功能
org.w3c.dom.events.Event 接口。常量：CAPTURING_PHASE, AT_TARGET, BUBBLING_PHASE（共 3 个）。 实例/声明方法：getType, getTarget, getCurrentTarget, getEventPhase, getBubbles, getCancelable, getTimeStamp, stopPropagation, preventDefault, initEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.Event() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.Event）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
黑盒行为覆盖（真实派发链路验证）：
- 常量表：CAPTURING_PHASE=1、AT_TARGET=2、BUBBLING_PHASE=3。
- initEvent 设置 type/bubbles/cancelable 且可重复初始化覆盖；派发之外 target/currentTarget 为 null、phase 归 0/AT_TARGET。
- 捕获→目标→冒泡全序：capture 阶段 currentTarget=祖先、target=目标；目标节点 phase=AT_TARGET 且 target==currentTarget；bubbles=false 时祖先只看到捕获阶段；派发中 getTimeStamp()>0。
- stopImmediatePropagation/getDefaultPrevented 在 Level 2 接口缺席。
- 版本相关实现事实（双侧一致，与 DOM 规范相反）：Xerces `dispatchEvent` 返回值反转——无 preventDefault 返回 false、preventDefault 后返回 true；未初始化事件派发 → `EventException UNSPECIFIED_EVENT_TYPE_ERR(0)`。
