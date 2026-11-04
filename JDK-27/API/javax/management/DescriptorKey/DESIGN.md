# javax.management.DescriptorKey 黑盒行为测试设计

## 1. API 功能
- 注解类型：`@DescriptorKey("units")`，`value()` 返回描述符字段名（无默认值）。

## 2. Java 用户如何使用
定义自定义注解时标注 value() 元素，把注解元素映射到 MBean 描述符字段。
```java
@Documented @Retention(RUNTIME) @Target(METHOD)
public @interface Units {
    @DescriptorKey("units")
    String value();
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/SqeDescriptorKey.java`、`test/jdk/javax/management/mxbean/SqeDescriptorKey.java`：DescriptorKey 注解样例。
- 实现权威：`src/java.management/share/classes/javax/management/DescriptorKey.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：反射读取方法上的 @DescriptorKey，value() 返回声明的字段名。
- 白盒：isAnnotation、RUNTIME 保留、value() 返回 String 且无默认值（getDefaultValue()==null）、带 @Target。
