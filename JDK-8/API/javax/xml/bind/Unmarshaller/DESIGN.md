# Unmarshaller 设计案（JDK-8 契约测试）

## 1. API 功能
XML→对象反序列化器：unmarshal 9 个原始通道 + 5 个带 Class 泛型通道、Listener、ValidationEventHandler、Schema、Adapter、AttachmentUnmarshaller。

## 2. 使用方式
ctx.createUnmarshaller() → unmarshal(source) 或 unmarshal(source, Class)（返回 JAXBElement）。事件处理器返回 false 时把事件升级为 UnmarshalException。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 通道矩阵：Reader/InputStream/File/URL/InputSource/Source/Node 全部回读 name==alice；unmarshal(Source,Class) 返回 **JAXBElement**（需 .getValue()）。
- Listener：before/after 各 >=1 次、顺序、null 移除。
- 事件矩阵：良构匹配输入不触发事件；错误根元素触发 event（severity ERROR/FATAL、locator line/column >=0）；handleEvent false → UnmarshalException；setEventHandler(null) 恢复默认。
- 负例：unmarshal(null Reader) → IAE；malformed → UnmarshalException 且 **cause=SAXParseException**；错误根（良构）→ UnmarshalException 且 **cause==null**（实测差异，规范未定）；未知属性 → PropertyException。
- 易错点：setValidating/isValidating 在 RI 2.x 抛 UnsupportedOperationException（勿断言布尔返回）；unmarshal 通道共 13 个重载（9 原始+4 泛型）。
