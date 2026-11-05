# javax.xml.transform.sax.TemplatesHandler — 设计案

## 1. API 功能
编译期 SAX handler:以 SAX 事件流喂样式表,getTemplates() 得到编译产物。方法:getTemplates()、setSystemId(String)、getSystemId()。

## 2. Java 用户如何使用
`TemplatesHandler th = saxFactory.newTemplatesHandler(); th.setResult(templatesResult 不需要); reader.setContentHandler(th); reader.parse(stylesheet); Templates t = th.getTemplates();`

## 3. 开源代码实际用法调研
JDK XSLTC 的 CompilerHandler 路径;test/jdk/jaxp 用例少,主要为嵌入 SAX 管线动态编译样式表设计。

## 4. 测试用例设计 + 易错细节
黑盒:systemId set/get round-trip;喂完样式表事件后 getTemplates() 非 null,newTransformer().transform() 输出精确。
真实闭环:用 namespace-aware 的 SAX parser(XMLReader,factory setNamespaceAware(true))解析内嵌样式表字符串喂入 → 编译成功。
易错:(1) parser 必须 namespace-aware,否则 XSLT 元素命名空间丢失、编译成垃圾模板(输出错误但不报错);(2) 非样式表 XML 喂入不抛异常——getTemplates() 返回非 null 的"默认规则"模板(探针锁定,断言必须写 != null 而非 == null);(3) systemId 在 parse 时自动从 InputSource 记录,getSystemId() 回读。
