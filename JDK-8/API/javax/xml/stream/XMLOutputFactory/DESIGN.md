# javax.xml.stream.XMLOutputFactory — 设计案（JDK-8）

## 1. API 功能
StAX 输出抽象工厂（newInstance/newFactory；createXMLStreamWriter(Writer/OutputStream/OutputStream,enc)；createXMLEventWriter 同三形态；setProperty/getProperty/isPropertySupported；IS_REPAIRING_NAMESPACES 常量）。

## 2. 使用方式
newFactory()/newInstance()；setProperty(IS_REPAIRING_NAMESPACES) 配置；createXMLStreamWriter/createXMLEventWriter 派生。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 属性契约
IS_REPAIRING_NAMESPACES：默认 false；true/false 往返；setProperty/getProperty 未知名 → IllegalArgumentException；isPropertySupported 矩阵；属性名常量 "javax.xml.stream.isRepairingNamespaces"。
### 修复命名空间（行为断言）
- 未声明前缀 writeStartElement("q","e","urn:q") → 自动补 `xmlns:q="urn:q"`（输出串 contains 且全等锁定 `<q:e xmlns:q="urn:q" q:a="1">t</q:e>`）。
- 重复 writeNamespace 同绑定被抑制（`<a xmlns:p="urn:p"><p:b></p:b></a>`）。
- 修复输出经 XMLInputFactory 回读树等价（闭环）。
### 创建通道
createXMLStreamWriter(Writer)/(OutputStream)/(OutputStream,enc)：Writer 与 OutputStream+UTF-8 输出**逐字符全等**；createXMLEventWriter(Writer)/(OutputStream,enc) 同理（事件工厂 StartDocument 写出 `<?xml version="1.0" encoding="UTF-8"?><e></e>`）。

### JDK-8 vs JDK-27 差异（本轮 javap/实测锁定）
- 工厂发现：JDK-27 三个工厂均有 `newDefaultFactory()`（行为断言已加）；JDK-8 无此静态方法（javap 确认），双侧 newInstance/newFactory 均可用且返回新实例。
- 实现类：双侧均为 `com.sun.xml.internal.stream.*`（JDK-27 位于 java.xml 模块内），行为面在 StAX 核心路径上一致。

### 断言计数
见 XMLOutputFactoryTest.java 末尾 PASS 输出（本轮深化后 21 断言，双侧只增不减）。
