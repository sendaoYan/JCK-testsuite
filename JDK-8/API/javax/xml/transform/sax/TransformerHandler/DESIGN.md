# javax.xml.transform.sax.TransformerHandler — 设计案

## 1. API 功能
运行期 SAX handler:边收事件边转换,输出流向已设置的 Result。接口方法(继承 ContentHandler/LexicalHandler/DTDHandler):getSystemId()/setSystemId()、getTransformer()、setResult(Result)(必须)。

## 2. Java 用户如何使用
`TransformerHandler h = factory.newTransformerHandler(tpl); h.setResult(new StreamResult(sw)); parser.setContentHandler(h); parser.parse(xml);` 或 XMLFilter 管线中间层。

## 3. 开源代码自动用法调研
JDK test/jdk/javax/xml/jaxp 管线用例;XSLTC TransletHandler 转发事件到结果 serializer。

## 4. 测试用例设计 + 易错细节
黑盒+真实闭环:(1) 基本管线——样式表 handler(Templates) 喂固定事件序列 → 输出尾部精确;(2) 参数注入——h.getTransformer().setParameter(...) 生效于输出;(3) 恒等模式 newTransformerHandler() 输出 == 输入;(4) systemId round-trip;(5) 复用——同一 handler 第一次转换完成后 setResult(新 Result) 再喂事件可再次输出(探针锁定 XSLTC 允许)。
易错:(1) 未 setResult 就发事件 → IllegalStateException("Can't transform more than once..." 家族);(2) getTransformer() 拿到的实例与 Templates.newTransformer() 语义一致,reset() 后可重配;(3) 事件必须完整(startDocument→endDocument),缺 endDocument 输出为空。
