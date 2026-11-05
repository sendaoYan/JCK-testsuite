# org.w3c.dom.css.CSSMediaRule — 设计案

## 1. API 功能
org.w3c.dom.css.CSSMediaRule 接口。实例/声明方法：getMedia, getCssRules, insertRule, deleteRule。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.css.CSSMediaRule() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.css.CSSMediaRule）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CSSMediaRuleTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CSSMediaRuleTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

- 生产者缺席（双侧实测一致）：`hasFeature("CSS"/"StyleSheets"/"Views","2.0")` 均为 false、`getFeature` 返回 null、Document/Element/DOMImplementation 不可 cast 到本模块接口——JDK 内置 Xerces 未实现 CSS/StyleSheets/Views 引擎。
- 降级策略：常量精确锁定 + 接口形状（方法名/签名/继承）锁定 + 匿名实现（或 Proxy）行为路由断言。
- getMedia/getCssRules/insertRule/deleteRule：insertRule 返回 index 且计数增长、deleteRule 对称；MEDIA_RULE(4)。
