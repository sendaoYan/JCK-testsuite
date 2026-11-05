# org.w3c.dom.CDATASection — 设计案

## 1. API 功能
org.w3c.dom.CDATASection 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.CDATASection() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.CDATASection）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CDATASectionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CDATASectionTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
身份：nodeName "#cdata-section"、nodeType 4、instanceof Text；CharacterData 编辑族全继承（append/substring/replace）；splitText 后**新半是普通 Text（type 3, #text）而非 CDATA**（split(0) 同理）；getWholeText 返回自身数据；replaceWholeText 归并后节点保持 CDATA 类型（与 Text 调用路径不同，实测）；createCDATASection("a]]>b") 合法（DOM 层不禁止）；解析侧：`<![CDATA[x < y & z]]>` 内容保持原始不转义、相邻实体引用文本照常展开、空 CDATA len 0；isEqualNode 同数据相等、CDATA 永不等于 Text。
