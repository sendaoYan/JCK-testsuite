# javax.management.ConstructorParameters 黑盒行为测试设计

## 1. API 功能
- 注解类型：`@ConstructorParameters({"init","used",...})`，`value()` 返回 getter 名数组（无默认值）。

## 2. Java 用户如何使用
标注在构造器上，把构造器参数映射到对象的 getter；JMX 内省时优先于 `@java.beans.ConstructorProperties`。
```java
@ConstructorParameters({"name", "count"})
public Sample(String name, int count) {}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/Introspector/LegacyConstructorPropertiesTest.java`：与 ConstructorProperties 的优先级。
- 实现权威：`src/java.management/share/classes/javax/management/ConstructorParameters.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用反射在构造器上取注解，value() 返回声明顺序的 getter 名。
- 白盒：isAnnotation、RUNTIME 保留、value() 返回 String[] 且无默认值（getDefaultValue()==null）。
