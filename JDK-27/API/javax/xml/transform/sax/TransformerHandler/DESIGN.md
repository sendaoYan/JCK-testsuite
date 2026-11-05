# javax.xml.transform.sax.TransformerHandler — 设计案

## 1. API 功能
javax.xml.transform.sax.TransformerHandler 接口。实例/声明方法：setResult, setSystemId, getSystemId, getTransformer。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.sax.TransformerHandler() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.TransformerHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerHandlerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 管线闭环三形态（Templates/Source 参数注入/identity）输出精确。
- systemId round-trip；第一次转换完成后 setResult(新) 可再次运行（探针锁定 XSLTC 允许复用）。
- 易错：未 setResult 先发事件 → IllegalStateException；事件必须 startDocument→endDocument 完整。
