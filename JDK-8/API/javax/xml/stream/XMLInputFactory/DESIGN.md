# javax.xml.stream.XMLInputFactory — 设计案（JDK-8）

## 1. API 功能
StAX 输入抽象工厂（newInstance/newFactory 双发现 + 系统属性覆盖；createXMLStreamReader(Reader/Source/InputStream/InputStream,enc)×4；createXMLEventReader(Reader/(sysId,Reader)/Source/InputStream/InputStream,enc)；createFilteredReader(Stream/Event)；setProperty/getProperty/isPropertySupported；setXMLResolver/getXMLResolver、setXMLReporter/getXMLReporter、setEventAllocator/getEventAllocator；9 个标准属性常量）。

## 2. 使用方式
newFactory()/newInstance() 发现实现；setProperty 配置；createXMLEventReader/createFilteredReader 派生。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 属性契约
- 5 个布尔属性（COALESCING/NAMESPACE_AWARE/REPLACING_ENTITY_REFERENCES/SUPPORTING_EXTERNAL_ENTITIES/SUPPORT_DTD）set/get 往返；isPropertySupported 9 个标准属性全 true、未知名 false。
- setProperty/getProperty 未知名 → IllegalArgumentException（工厂层严格）。
- **易错**：IS_VALIDATING=true → **IllegalArgumentException**（"true value of isValidating not supported"，StAX 不做 DTD 验证——JDK 直接拒绝而非"无效果"；false 可设）。规范允许"置 true 无效果"，JDK 选择了拒绝，实测锁定。
- 9 个标准属性名常量逐字断言（javax.xml.stream.isCoalescing 等）。
### 创建通道
Reader / InputStream / (InputStream,enc) / Source 四路 createXMLStreamReader + XMLEventReader 三形态。decl 编码经 Reader 时只反映在 getCharacterEncodingScheme，getEncoding 为 null；InputStream 时 getEncoding 为真实探测值；(InputStream,enc) 提示时 scheme=null。
### 行为开关（读侧行为断言）
- IS_COALESCING=true：文本+CDATA+实体合并成单个 CHARACTERS（"xyz&w"）。
- IS_NAMESPACE_AWARE=false：不拆 qname（localName="p:a"，ns=null）。
- IS_REPLACING_ENTITY_REFERENCES=false：内部实体产生 ENTITY_REFERENCE(9) 事件（getLocalName=实体名，getText=替换文本）。
- SUPPORT_DTD=false + 实体引用 → XMLStreamException（实体未声明）。
### createFilteredReader
StreamFilter（只留元素事件：文本/注释被滤除，END_DOCUMENT 仍送达；**根元素 START 在过滤器 priming 时被消费**——序列 [Sb,Eb,Ea,EV8]）与 EventFilter（只留 StartElement：[a,b]）；reject-all 得零事件。矩阵见 EventFilter/StreamFilter 测试。
### 发现机制
newFactory/newInstance 每次新实例、同一实现类；系统属性 javax.xml.stream.XMLInputFactory 可覆盖（finally 恢复）。

### JDK-8 vs JDK-27 差异（本轮 javap/实测锁定）
- 工厂发现：JDK-27 三个工厂均有 `newDefaultFactory()`（行为断言已加）；JDK-8 无此静态方法（javap 确认），双侧 newInstance/newFactory 均可用且返回新实例。
- 实现类：双侧均为 `com.sun.xml.internal.stream.*`（JDK-27 位于 java.xml 模块内），行为面在 StAX 核心路径上一致。

### 断言计数
见 XMLInputFactoryTest.java 末尾 PASS 输出（本轮深化后 55 断言，双侧只增不减）。
