# QName — 设计案

## 1. API 功能
XML 限定名，Serializable。构造 (ns,local)/(ns,local,prefix)/(local)；getNamespaceURI/getLocalPart/getPrefix；equals/hashCode；toString；valueOf(String)。

## 2. Java 用户如何使用
new QName("urn:x","local","pre") 读三属性；valueOf("{urn:x}local") 解析。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/namespace 校验 QName 构造、equals 忽略前缀、valueOf 解析。

## 4. 测试用例设计 + 自动化实现
黑盒：三/二/一参构造与 toString（{ns}local）；null 语义（null ns→空串、null local→IAE）；equals/hashCode 忽略前缀；valueOf 带/不带大括号与 null→IAE；Serializable。

## 5. 深化（本批新增）
- 与 JDK-8 侧相同条目（equals 无 prefix/valueOf 矩阵/null→IAE/空 local 合法/序列化 round-trip 含 prefix/svuid/不可变与 final equals）。
- 双侧实测无差异（含 helpful NPE 消息——两侧均抛 IAE 而非 NPE）。
