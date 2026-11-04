# javax.management.MXBean 黑盒行为测试设计

## 1. API 功能
- 注解类型：`@MXBean`，`value()` 返回 true/false（默认 true），标记 MXBean 接口。

## 2. Java 用户如何使用
标注接口为 MXBean，使属性/操作类型映射为开放类型（如 List<String> 映射为 String[]）。
```java
@MXBean
public interface CacheMXBean { List<String> getNames(); }
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/mxbean/MXBeanAnnotationTest.java`、`MXBeanFlagTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MXBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：`JMX.isMXBeanInterface` 对 @MXBean 接口为 true；@MXBean(false) 时 value() 返回 false；注解可在接口上反射读取。
- 白盒：isAnnotation、RUNTIME 保留、value() 返回 boolean 且默认 true。
