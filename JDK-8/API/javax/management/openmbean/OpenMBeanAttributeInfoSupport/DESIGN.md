# javax.management.openmbean.OpenMBeanAttributeInfoSupport — 测试设计

## 1. API 功能
OpenMBeanAttributeInfo 实现（extends MBeanAttributeInfo）。构造形态：六参 (name,desc,openType,
readable,writable,isIs) + 可选 (defaultValue)/(defaultValue,min,max)/(defaultValue,T[] legal)/
(+Descriptor)。isIs 校验：isIs 且不可读 → IllegalArgumentException("Cannot have an \"is\" getter
for a non-readable attribute")；isIs 且 openType 非 SimpleType.BOOLEAN → IllegalArgumentException
("Cannot have an \"is\" getter for a non-boolean attribute")。

## 2. Java 用户典型用法
开放 MBean 属性声明；读写/只读/isIs 三形态；equals 含三布尔与 descriptor。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码 static check()（消息原文）。
- OpenJDK test/jdk/javax/management/openmbean（equals/校验思路）。

## 4. 测试用例设计 + 易错细节（OpenMBeanAttributeInfoSupportTest，批 G：104→124 断言）
- isIs 双负例精确消息（见上）；isIs 合法形态读写布尔。
- **跨类 equals 不对称**：param.equals(attr)==true 而 attr.equals(param)==false（attr 多三布尔
  参与比较）——双侧锁定。
- equals：default/min/max/legal/三布尔逐项参与；getDescriptor 参与 Attribute 层比较。
- 序列化 SUID 锁定；各形态 getter 组合。
- 版本差异：两侧零差异。
