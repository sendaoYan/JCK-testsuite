# javax.xml.stream.XMLResolver — 设计案

## 1. API 功能
javax.xml.stream.XMLResolver 接口。实例/声明方法：resolveEntity。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLResolver() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLResolverTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：拦截闭环）
- 外部实体拦截：resolver 被调（(null, systemId, null, null)）+ 返回 ByteArrayInputStream → 替代内容 `<sub>RESOLVED</sub>` 真实进树（事件序列 [11,1,1,4,2,2,8]、文本 RESOLVED）——离线闭环。
- 内部实体不经过 resolver（计数 0）；返回 null 回退文件装载确定性失败（XMLStreamException）；默认设置下未展开的外部实体离线可解析。
