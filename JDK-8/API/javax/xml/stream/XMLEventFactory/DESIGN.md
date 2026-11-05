# javax.xml.stream.XMLEventFactory — 设计案（JDK-8）

## 1. API 功能
事件对象工厂（newInstance/newFactory/setLocation；createStartElement/createEndElement 双形态、createAttribute×3、createNamespace×2、createCharacters/createCData/createComment/createProcessingInstruction/createDTD/createEntityReference/createSpace/createIgnorableSpace/createStartDocument×3/createEndDocument）。

## 2. 使用方式
newFactory()；createXxx 造事件后交 XMLEventWriter 写出或直接断言。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### create 全家族
StartElement/EndElement 的 (prefix,ns,local) 与 (QName, Iterator, Iterator) 双形态；Attribute 三形态（QName、(p,ns,l,v)、(l,v)——无 ns 时 QName ns 为 ""）；Namespace 单参（默认 ns，prefix==""）双参；createCData（Characters 型 + isCData）；createDTD（getDocumentTypeDeclaration 原文；**getEntities()/getNotations() 返回 null**——实测）；createEntityReference(name,null)；createSpace/createIgnorableSpace（isWhiteSpace/isIgnorableWhiteSpace）。
### 事件类型码
工厂造事件 getEventType：ch=4/cdata=4(以 isCData 区分)/cm=5/ns=13/attr=10/dtd=11/ent=9/pi=3/ed=8/start=1/end=2；isXxx 家族布尔。
### setLocation
setLocation(loc) 影响其后创建的事件（getLocation()==loc 同一实例）；isSpecified() 工厂造 Attribute 上为 **false**（解析器侧为 true——两侧差异，实测锁定）。
### 闭环
create → XMLEventWriter 写出（含 DOCTYPE 内部子集声明实体，使 EntityReference 回读合法）→ XMLInputFactory 回读文本含实体展开值 "txtcdEE"。
### 发现机制
newFactory/newInstance 新实例；系统属性 javax.xml.stream.XMLEventFactory 覆盖（finally 恢复）。

### 断言计数
见 XMLEventFactoryTest.java 末尾 PASS 输出（本轮深化后 53 断言，双侧只增不减）。
