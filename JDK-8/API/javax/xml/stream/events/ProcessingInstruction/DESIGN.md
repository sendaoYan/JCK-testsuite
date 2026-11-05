# javax.xml.stream.events.ProcessingInstruction — 设计案

## 1. API 功能
处理指令事件接口（extends XMLEvent）。方法：getTarget、getData。无 getPITarget（那是 XMLStreamReader 的方法面）。

## 2. Java 用户如何使用
工厂 createProcessingInstruction(target, data)；解析：序言与元素内 `<?t d?>` 均产生 PROCESSING_INSTRUCTION(3)。

## 3. 开源代码实际用法调研
JDK 实现 ProcessingInstructionEvent；JDK test/jdk javax/xml/stream；本仓库 ProcessingInstructionTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 工厂：target/data 全等；事件码 3 + isProcessingInstruction；编码 `<?t d?>` 全等；
  data=null 容忍且编码为 `<?t?>`；target=null 容忍。
- 解析：序言 `<?pi1 data1?>` 与元素内 `<?in mid?>` 的 target/data 精确；
  无数据形态 `<?tgt?>` 解析侧 getData()=""（空串），与工厂 null 不同 —— 两侧锁定。
- 闭环：createProcessingInstruction("xsl","transform") → XMLEventWriter → 重新解析回读 target/data 全等。
- 错误窄化 asCharacters → CCE。
