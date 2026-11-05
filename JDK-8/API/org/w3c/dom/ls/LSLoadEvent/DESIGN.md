# org.w3c.dom.ls.LSLoadEvent — 设计案

## 1. API 功能
LSLoadEvent 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 形状：extends Event；仅 getNewDocument/getInput 两方法；无数值常量。
- 实例经 java.lang.reflect.Proxy 构造并验证 getter 路由（匿名实现需实现全部 Event 方法，Proxy 更干净）。
- 生产者缺席：同步解析不产生 LSLoadEvent（async-only 通知）。