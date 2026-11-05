# javax.management.DescriptorKey 黑盒行为测试设计

## 1. API 功能
- 元注解值类型：`@DescriptorKey("字段名")` 标注自定义注解的元素，声明该元素值进入 MBean 描述符。

## 2. Java 用户如何使用
```java
@Retention(RUNTIME) @Target(METHOD)
public @interface Units {
    @DescriptorKey("units") String value();
}
```
标准 MBean 接口方法标注后，server.getMBeanInfo 的描述符中出现该字段。

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/introspector/...`（SqeDescriptorKey 系）。
- 实现权威：`src/java.management/share/classes/javax/management/DescriptorKey.java` + `Introspector.descriptorForElement`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：注解反射 + 真实闭环。
- 白盒：@Target/@Retention/@Documented 元事实。

## 5. 深化（批 B 追加）
- 元事实：@Target 恰为 {METHOD}；@Documented；RUNTIME 保留；value() 返回 String 且无默认值。
- 【真实闭环（重点）】自定义 @CacheSpec（units String / defVal int→Integer / cls Class→getName() / mode enum→name() / tags String[]）标注 Standard MBean 接口 getter；MBeanServerFactory.newMBeanServer() + registerMBean 后，getMBeanInfo().getAttributes() 中 Count 属性描述符出现全部五个字段（bytes / Integer(5) / "java.lang.Thread" / "FAST" / [a,b]）。
- 操作方法（@OpUnits）与操作参数（@Target(PARAMETER) 的 @ParamUnits）同样进入各自描述符；未标注参数无该字段。
- 一致性规则：getter 与 setter 对同一描述符字段给出不同值 → registerMBean 抛 NotCompliantMBeanException，cause 为 IllegalArgumentException "Inconsistent values for descriptor field units: g :: s"。
- 同一 server 上 invoke 正常工作。
- 版本差异：无。
