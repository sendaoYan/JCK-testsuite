# javax.naming.BinaryRefAddr — 测试设计

## 1. API 功能
字节数组地址：ctor(type, byte[]) 与 ctor(type, byte[], off, len)；equals 比较内容而非数组身份。

## 2. Java 用户如何使用
存放序列化字节/证书等二进制配置；`new BinaryRefAddr("cert", derBytes)` 放入 Reference，工厂端 `(byte[]) addr.getContent()` 重建。

## 3. 开源代码实际用法调研
- JDK `javax/naming/BinaryRefAddr.java`：toString 以十进制空格分隔字节输出（实测 "Address Type: bt\\nAddressContents: 1 2 3 "）。
- CORBA/JNDI 提供者（如旧版 com.sun.jndi.cosnaming）用 BinaryRefAddr 携带 IOR 字节。

## 4. 测试用例设计（BinaryRefAddrTest.java）
- 白盒：extends RefAddr、两个构造器。
- 黑盒：内容往返；**构造器防御性拷贝**（改入参数组不影响）但 getContent 返回内部视图（修改可见，实测）；offset/length 构造器切子数组；equals/hashCode 按内容；toString 前缀与字节列表；空数组。
