# javax.naming.RefAddr — 测试设计

## 1. API 功能
抽象地址类型：`getType/getContent/equals/hashCode/toString`；protected 构造器(String type)；Serializable。

## 2. Java 用户如何使用
不直接实例化，用 StringRefAddr/BinaryRefAddr 子类放入 Reference；`ref.get("url").getContent()` 取回配置。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/javax/naming/RefAddr/NullContent.java`（JDK-4347817）验证 null content 的 toString/hashCode 不抛 NPE。
- DataSource/ConnectionFactory JNDI 工厂（Tomcat Dbcp / OpenJPA）以 RefAddr 保存配置项。

## 4. 测试用例设计（RefAddrTest.java）
- 白盒：abstract、protected ctor、两个具体子类继承关系、方法清单。
- 黑盒：通过 StringRefAddr 验证抽象契约：getType/getContent/toString 格式、equals 对称与 type/content 判据、hashCode 一致性、null content 容忍。
