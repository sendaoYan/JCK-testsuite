# javax.management.openmbean.OpenMBeanConstructorInfoSupport — 测试设计

## 1. API 功能
OpenMBeanConstructorInfo 实现（extends MBeanConstructorInfo）。构造 (name,desc,OpenMBeanParameterInfo[][,Descriptor])；
null 签名 → 空数组；getSignature 每次新数组、元素共享；equals = name+signature（不含 desc）。

## 2. Java 用户典型用法
开放 MBean 构造器声明；与 OpenMBeanInfoSupport 组装。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码；双侧 javap。

## 4. 测试用例设计 + 易错细节（OpenMBeanConstructorInfoSupportTest，批 G：46→57 断言）
- 签名归一化（null→0）、防御拷贝双通道（getter 新数组、构造拷贝）、元素共享。
- equals：desc 不参与、签名长度/内容参与；序列化 SUID 锁定。
- 版本差异：两侧零差异。
