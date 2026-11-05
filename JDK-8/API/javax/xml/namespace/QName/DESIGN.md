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
- equals 核心契约：只看 ns+local，prefix 不参与（(u,a,p1).equals((u,a,p2))==true，hashCode 相等，对称）；不同 ns 或 local 破坏 equals；equals(null)/equals(String) false。
- null/空矩阵（双侧一致）：**(ns,null)/(null)/(ns,null,p) → IllegalArgumentException（不是 NPE）**，消息分别为 "local part cannot be \"null\" ..." 与 "prefix cannot be \"null\" ..."；3 参 null ns → 归一 ""；**空串 localPart 双侧合法**（new QName("")/new QName(ns,"") 均接受，仅 null 被拒——锁定）。
- valueOf 矩阵："{urn:x}p:y" → local="p:y"（冒号保留、prefix ""）；"{a}}b" → 在第一个 } 切分 local="}b"；"y"/"}"/""/"{ns}" 双侧接受（"" local 不抛）；"{x"/"{" → IAE 消息 'cannot create QName from "{x", missing closing "}"'；"{}"/"{}x" → IAE 消息含 NULL_NS_URI；valueOf(null) → IAE。
- 序列化：writeObject→readObject round-trip equals + ns/local/**prefix 精确保留** + hashCode/toString 相等 + 反序列化类即 QName 本身（**无 writeReplace/代理**，NoSuchMethodException 实证）；serialVersionUID==-9120448754896609940（ObjectStreamClass 锁定）。
- 不可变：无任何 public set*/clear*；equals/hashCode 均为 final（子类不可改写相等语义）；**类本身非 final**。
- toString 从不含 prefix；无 ns 时输出裸 local。
