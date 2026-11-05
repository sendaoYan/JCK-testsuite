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

## 5. 深化（批 B 追加）
- 元事实：@Target 恰为 {ElementType.CONSTRUCTOR}（对方法不适用）、@Documented、RUNTIME、value() 返回 String[] 且无默认。
- 【真实闭环】MXBean：裸 server.invoke 返回 CompositeData（x=3,y=4）；JMX.newMXBeanProxy 通过 @ConstructorParameters({"x","y"}) 构造器重建 Point(3,4)，与本地 Point equals。
- 优先级：与 @java.beans.ConstructorProperties（相反顺序 {"y","x"}）并存时 ConstructorParameters 胜出（重建仍 x=3,y=4）。
- 【版本差异】@since 9：JDK-8 无 javax.management.ConstructorParameters（该目录仅 JDK-27 存在）；JDK-8 侧等价能力用 java.beans.ConstructorProperties。
