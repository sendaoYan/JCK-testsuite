# org.w3c.dom.css.CSSRuleList — 设计案

## 1. API 功能
CSSRuleList 契约（CSS 模块）。

## 2. Java 用户如何使用
JDK 未实现 CSS 引擎：无法从 DOM 获得 CSSRuleList 实例——常量/形状经反射或直接引用锁定，行为经匿名实现/Proxy 路由验证。

## 3. 参考来源
DOM Level 2 CSS 规范 + JDK 接口定义实测。

## 4. 测试用例设计 + 易错细节
- 生产者缺席（双侧实测一致）：`hasFeature("CSS"/"StyleSheets"/"Views","2.0")` 均为 false、`getFeature` 返回 null、Document/Element/DOMImplementation 不可 cast 到本模块接口——JDK 内置 Xerces 未实现 CSS/StyleSheets/Views 引擎。
- 降级策略：常量精确锁定 + 接口形状（方法名/签名/继承）锁定 + 匿名实现（或 Proxy）行为路由断言。
- getLength/item 索引访问；越界 item 返回 null 不抛。
