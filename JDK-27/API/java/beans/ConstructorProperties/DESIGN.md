# java.beans.ConstructorProperties — 设计案

## 1. API 功能
注解（@Target CONSTRUCTOR）。`String[] value()` 声明构造器参数名。

## 2. Java 用户如何使用
`@ConstructorProperties({"a","b"})` 标注构造器，供 Introspector/序列化识别参数名。

## 3. 开源代码实际用法调研
JDK 源码 `ConstructorProperties.java`；Jackson/Gson 读取参数名。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型 + value 返回 String[]。黑盒（`testBlackBox`）：反射读取注解 value。

---

# 深化补充（2026-11-05，batch beansC）

> 注：上文「供 Introspector 识别参数名」表述需限定——Introspector **不消费**该注解（属性集不变）；实际消费者是 `DefaultPersistenceDelegate`/`XMLEncoder` 持久化栈及第三方序列化库。

## 一、新增覆盖
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 结构：RUNTIME retention；Target 恰为 {CONSTRUCTOR}；Documented；value() 为 String[] 且**无默认值**；单元素 | testWhiteBox |
| P0 | 注解反射：{"a","b"} 内容/长度/顺序、两次调用一致、无注解构造器返回 null | testAnnotationReflection |
| P0 | 行为：CPBean(7,"seven") 编码零异常，XML 含 `<object class>`、`<int>7</int>`、`<string>seven</string>`；round-trip 两字段保真 | testRoundTrip |
| P0 | 行为：注解构造器与无参构造器并存时仍走注解构造器（无 `<void` setter 语句） | testAnnotatedCtorPreferred |
| P1 | 负例：`@ConstructorProperties({})` → ExceptionListener 捕获 InstantiationException，XML 无 object，读空文档 → ArrayIndexOutOfBoundsException | testEmptyValue |
| P1 | 负例：错名 "nope" → 捕获 IllegalStateException（含属性名）+ IllegalArgumentException，XML 无 object | testWrongPropertyName |
| P1 | Introspector：属性集恰 {a,b,class}，构造器注解不增删属性 | testIntrospectorUnaffected |

## 二、参考来源
- Oracle Java SE 27 javadoc `java/beans/ConstructorProperties.html`、`java/beans/DefaultPersistenceDelegate.html`；OpenJDK27 `test/jdk/java/beans/XMLEncoder` 失败路径思路。

## 三、易错规范细节
- JDK-27 与 JDK-8 的失败异常**类型一致**（IllegalStateException → IllegalArgumentException → discard Exception；读空文档 ArrayIndexOutOfBoundsException），仅消息文本不同（JDK-27 的 IAE 附带内部 NPE 详情）——断言按类型出现性写。
- 测试用自定义 ExceptionListener 收集异常，避免默认监听器向 stderr 打 "Continuing..." 噪声。
- 其余同 JDK-8 侧：DPD 走构造器路径时不生成 setter 语句；样例 Bean 字段不可 final 又带 setter。
