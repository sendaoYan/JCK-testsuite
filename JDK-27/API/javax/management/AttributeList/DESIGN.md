# javax.management.AttributeList 黑盒行为测试设计

## 1. API 功能
- 继承 `ArrayList<Object>`。
- 方法：`add(Attribute)`、`add(int, Attribute)`、`set(int, Attribute)`、`addAll(AttributeList)`、`asList()`、`get(int)`。

## 2. Java 用户如何使用
MBeanServer 的 `getAttributes`/`setAttributes` 返回/接收 AttributeList，用于批量读写属性。
```java
AttributeList al = mbs.getAttributes(on, new String[]{"Count", "Name"});
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/AttributeListTypeSafeTest.java`：类型安全的 add/set。
- `test/jdk/javax/management/MBeanServer/MBeanTest.java`：批量属性读写。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeList.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：add(Attribute)/add(int,Attribute)/set(int,Attribute) 生效；asList 返回 List<Attribute>；get 返回对应对象；继承 ArrayList 的 size/clear 行为；addAll(AttributeList) 合并。
- 白盒：public 类、父类 ArrayList、含 asList。
