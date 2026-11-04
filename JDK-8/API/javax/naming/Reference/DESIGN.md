# javax.naming.Reference — 测试设计

## 1. API 功能
对象的可存引用：className + 可选 factory 类/位置 + 有序 RefAddr 列表；`add/add(int)/remove/get(type)/getAll/size/clear/clone/equals`；实现 Serializable。

## 2. Java 用户如何使用
无法直接序列化的对象实现 Referenceable 返回 Reference，Context.bind 存引用；lookup 端 NamingManager.getObjectInstance 用 factoryClassName+location 重建对象。链路：构造 → add 多个地址 → get(type) → clone → equals。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/javax/naming/RefAddr/NullContent.java` 构造 Reference("com.sun.test.Class", addr) 并 add。
- `com.sun.jndi.rmi.registry.RegistryContext` 与 RMI 绑定路径以 Reference 描述可重建对象。
- Tomcat/OpenLiberty 数据源 JNDI 部署描述符生成 Reference(className, factory, location) 供 ResourceFactory 使用。

## 4. 测试用例设计（ReferenceTest.java）
- 白盒：public、Serializable、3 个构造器、方法清单。
- 黑盒：className/factory/location 三元组；add+add(0) 顺序；按 type 取地址/未知名 null；remove 返回值与 size；
  clear；getAll 枚举；clone equals 且元素共享（Vector clone 语义）、结构独立；equals 内容比较（className 不同/退化 null、非同类）；toString 前缀。
