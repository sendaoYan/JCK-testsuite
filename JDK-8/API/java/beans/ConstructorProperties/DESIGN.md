# java.beans.ConstructorProperties — 设计案（JDK-8）

> 覆盖目标：注解结构契约（RUNTIME retention、Target({CONSTRUCTOR}) 精确、@Documented、单元素 String[] value() 无默认值）+ 持久化行为契约（DefaultPersistenceDelegate 优先走注解构造器、round-trip 字段保真、空数组/错名的失败路径与精确异常）+ Introspector 属性集不受影响。headless 安全。

## 一、API 功能
- 构造器级注解，`String[] value()`（无默认值），声明「构造器形参 → 属性名」的对应关系。
- 被 `java.beans.DefaultPersistenceDelegate` 消费：以注解构造器实例化对象，按属性当前值生成构造器实参。

## 二、使用方式
- 无默认构造器、字段 final 或构造即定型的不可变 Bean：在构造器上标注 `@ConstructorProperties({"a","b"})`，XMLEncoder 即可持久化。
- Jackson/Gson 等亦读取该注解获取构造器参数名（参数名在字节码不可得时的替代）。

## 三、参考来源
- Oracle Java SE 8 javadoc `java/beans/ConstructorProperties.html` 与 `java/beans/DefaultPersistenceDelegate.html`（构造器路径规则）。
- JavaBeans 持久化规范（Long-Term Persistence, JSR-57）。
- OpenJDK8u `test/jdk/java/beans/XMLEncoder`（ConstructorProperties 用例的失败路径思路）。

## 四、测试用例设计 + 易错规范细节
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 结构：isAnnotation、Annotation 派生、declares value（原有用例保留） | testStructure |
| P0 | RUNTIME retention；Target 恰为 {CONSTRUCTOR}；Documented；value() 返回 String[] 且无默认值；单元素 | testWhiteBox |
| P0 | 注解反射：{"a","b"} 内容/长度/顺序、两次调用一致、无注解构造器返回 null | testAnnotationReflection |
| P0 | 行为：CPBean(7,"seven") 编码零异常，XML 含 `<object class="...$CPBean">`、`<int>7</int>`、`<string>seven</string>`；解码 round-trip 两字段保真 | testRoundTrip |
| P0 | 行为：注解构造器与无参构造器并存时**仍走注解构造器**（无 `<void` setter 语句），round-trip 保真 | testAnnotatedCtorPreferred |
| P1 | 负例：`@ConstructorProperties({})` → ExceptionListener 捕获 InstantiationException，XML 无 object 元素，读空文档 → ArrayIndexOutOfBoundsException | testEmptyValue |
| P1 | 负例：属性名不存在（"nope"）→ 捕获 IllegalStateException（消息含属性名）+ IllegalArgumentException，XML 无 object | testWrongPropertyName |
| P1 | Introspector：属性集恰为 {a,b,class}，构造器注解不增删属性 | testIntrospectorUnaffected |

### 易错规范细节
- **编码失败不抛异常**：XMLEncoder 走 ExceptionListener（默认打印 "Continuing..."），产物是**空 `<java>` 文档**；随后 `readObject()` 才以 ArrayIndexOutOfBoundsException 失败（实测 JDK-8/JDK-27 同型，消息文本不同）。
- 错名异常链：先 `IllegalStateException: Could not find property by the name <name>`，再 `IllegalArgumentException`（内部 NPE 包装，JDK-27 消息更详细），最后 discard 语句的 `Exception`；断言按**类型出现性**写，勿断言顺序与消息全文。
- 编码时 DPD 用构造器路径**不再生成 setter 语句**（`<void method=...>` 消失），可用 `!xml.contains("<void")` 断言路径选择。
- 样例 Bean 字段不能既 final 又提供 setter（编译错）；构造器与 setter 二选一语义由 DPD 决定。
- 测试需 `enc.setExceptionListener(...)` 收集异常避免默认 stderr 噪声，并保证断言可枚举。
