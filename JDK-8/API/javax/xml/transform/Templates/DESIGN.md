# javax.xml.transform.Templates — 设计案

## 1. API 功能
编译后的样式表(线程安全、可重用)。接口方法:newTransformer()、getOutputProperties()(快照)。

## 2. Java 用户如何使用
`Templates t = factory.newTemplates(new StreamSource(xsl)); Transformer a = t.newTransformer();` 每次转换取新 Transformer,参数互不影响;预编译一次多处复用。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的复用/线程安全用例;XSLTC 的 TemplatesImpl 持 translet 字节码。

## 4. 测试用例设计 + 易错细节
黑盒+真实闭环:(1) 复用隔离——同一 Templates 两次 newTransformer() 是不同实例,各自 setParameter("p","AA"/"BB") 输出分别精确 `<e p="AA">3</e>`/`<e p="BB">3</e>`,未设参默认 `<e p="">3</e>`;同一 transformer 两次 transform 确定性一致;(2) 输出属性快照——样式表 xsl:output method="text" indent="yes" 在 getOutputProperties() 可见;修改返回的 Properties 副本不影响后续 newTransformer().getOutputProperties()(快照语义);(3) Templates 本身可序列化(TemplatesImpl),但接口契约只承诺上述两方法。
易错:(1) getOutputProperties 返回的是防御拷贝,内部状态不可经它篡改;(2) 参数是 Transformer 级不是 Templates 级——Templates 无 setParameter。
