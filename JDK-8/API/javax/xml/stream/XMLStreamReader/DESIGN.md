# javax.xml.stream.XMLStreamReader — 设计案（JDK-8）

## 1. API 功能
拉模型游标读接口（44 方法：next/nextTag/getElementText/require 事件推进，getName/getLocalName/getPrefix/getNamespaceURI 命名空间，getAttribute* 属性族，getText/getTextCharacters 文本族，getPITarget/getPIData，getLocation，getVersion/getEncoding/getCharacterEncodingScheme/isStandalone/standaloneSet XMLDecl 反映，close/hasNext/getProperty）。实现 XMLStreamConstants。

## 2. 使用方式
XMLInputFactory.createXMLStreamReader(...) 获得；循环 next() 按事件码分派；或 nextTag/getElementText 便捷推进。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 核心闭环：事件状态机整表（固定文档 → next() 事件码序列锁定）
文档含 XMLDecl(standalone=yes)、PI、注释、前缀 ns + 默认 ns、实体 &amp;&lt;、字符引用 &#65;&#x4E2D;、CDATA、嵌套、空元素、混合空白。
- 初始态 `getEventType()==7`；`getVersion()=="1.0"`、`getCharacterEncodingScheme()=="UTF-8"`（来自 decl）、`getEncoding()==null`（Reader 输入）、`isStandalone()/standaloneSet()==true`。
- next() 序列精确锁定：`[3,5,1,4,4,4,4,4,1,4,2,4,1,2,4,4,4,4,2,8]`（PI→注释→root SE→5 段 CHARACTERS→child SE→CDATA 体→child EE→ws→empty SE→empty EE→ws→A→中→ws→root EE→ED）。
- **易错 1**：CDATA 节读侧在 JDK StAX 上报为 **CHARACTERS(4)** 而非 CDATA(12)（`getText()` 给出完整 CDATA 体；coalescing 下亦并入文本）——两版本一致，按实测锁定。
- **易错 2**：实体把文本切成多个 CHARACTERS（"text"/"&"/"more"/"<" 各一段），不断言段数、只断言拼接全等。
- **易错 3**：空元素读侧产生 START_ELEMENT+END_ELEMENT 两个事件（非单事件）。
- **易错 4**：xmlns 声明不计入 getAttributeCount（nsCount 计数）；属性序保持文档序；type 为 "CDATA"；`getAttributeValue(null, local)` 的 null ns 在 JDK 实现上匹配任意命名空间（规范语义为无 ns，按实测锁定）。
### getElementText / nextTag / require
- getElementText：实体+CDATA+字符引用拼接全等（`a&b<c>A中`），跳过注释与 PI；流侧调用后游标停在 END_ELEMENT 上。
- **易错 5**：getElementText 在非 START_ELEMENT 调用，javadoc 声称 IllegalStateException，**实测抛 XMLStreamException**（"parser must be on START_ELEMENT to read next text"）——两版本一致，测试两者皆容。
- nextTag：跳空白/注释/PI；遇非空白文本抛 XMLStreamException（"found: CHARACTERS, expected START_ELEMENT or END_ELEMENT"）。
- require：匹配静默通过；不匹配抛 XMLStreamException（LocalName mismatch）。
### 生命周期与负例
- next() 越过 END_DOCUMENT → NoSuchElementException；hasNext()=false。
- **易错 6**：close() 后 next()/hasNext() **仍可继续解析**（close 不使 reader 失效）——实测锁定。
- 格式错 → XMLStreamException，携带 Location（行号≥1）。
- `getProperty(未知名)`：javadoc 称 IAE，**实测返回 null**（与工厂层相反——工厂层确实抛 IAE）。
- getPITarget 在非 PI 事件 → IllegalStateException；`<?solo?>` 无数据 → getPIData()==""。
- DTD 事件(11) 的 getText 是完整 doctype 声明；默认内部实体被展开为 CHARACTERS。
### 命名空间矩阵
getNamespaceContext：前缀/默认/xml 前缀/未知四路；getNamespaceCount==声明数；子元素继承 ns；getNamespaceURI(prefix) 直查。IS_NAMESPACE_AWARE=false 时 localName 变为 "p:a"、ns=null（实测有生效）。

### 断言计数
见 XMLStreamReaderTest.java 末尾 PASS 输出（本轮深化后 125 断言，双侧只增不减）。
