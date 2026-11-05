# javax.xml.transform.sax.TemplatesHandler — 设计案

## 1. API 功能
javax.xml.transform.sax.TemplatesHandler 接口。实例/声明方法：getTemplates, setSystemId, getSystemId。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.sax.TemplatesHandler() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.TemplatesHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TemplatesHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TemplatesHandlerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 真实闭环：namespace-aware parser 喂内嵌样式表 → 编译成功，输出精确。
- 非样式表 XML → getTemplates() 非 null（默认规则模板，探针锁定）。
- systemId 生命周期：parse 时自动记录自 InputSource，getSystemId() 回读。
- 易错：parser 非 namespace-aware 时 XSLT 命名空间丢失、编译成垃圾模板。
