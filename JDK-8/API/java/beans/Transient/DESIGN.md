# java.beans.Transient — 设计案（JDK-8）

> 覆盖目标：注解结构契约（RUNTIME retention、Target({METHOD}) 精确、单元素 `value()`、默认值 true、非 Documented）+ 与 Introspector 的行为契约（**属性仍被发现**，但描述符带 `transient=true/false` 值属性）+ getter/setter 冲突优先级（JDK-8 setter 胜出）+ 版本差异（BeanProperty/JavaBean 在 JDK-8 不存在）。headless 安全。

## 一、API 功能
- 方法级注解，`boolean value() default true`（默认值实测为 true，非 javadoc 常见误传的 false）。
- 作用于 getter/setter：让 Introspector 在对应 `PropertyDescriptor` 上设置键为 `"transient"` 的值属性（`setValue`），布尔包装类型。
- **注解不会删除属性**：属性照常出现在 `getPropertyDescriptors()` 里。

## 二、使用方式
- `@Transient` 或 `@Transient(true)` 标注 getter/setter，向持久化工具（XMLEncoder 等）声明该属性不参与持久化；`@Transient(false)` 显式关闭。

## 三、参考来源
- Oracle Java SE 8 javadoc `java/beans/Transient.html`（retention/target/默认值）。
- JavaBeans 规范 1.01-A（transient 属性概念）。
- OpenJDK8u `java/beans/Introspector.getTargetPropertyInfo`（transient 属性注入实现）与对应回归思路。

## 四、测试用例设计 + 易错规范细节
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 结构：isAnnotation、Annotation 派生、单元素 value()（原有用例保留） | testStructure |
| P0 | RUNTIME retention；Target 恰为 {METHOD}；value() 返回原始 boolean；默认值 true（实测）；非 Documented | testWhiteBox |
| P0 | 注解读取：getter 上 true/false、未标注方法 null、代理 equals、isAnnotationPresent | testAnnotationValues |
| P0 | 行为：@Transient(true) getter → 属性存在 + `getValue("transient")==Boolean.TRUE` + 读写方法/类型不变 | testIntrospectorBehavior |
| P0 | 行为：@Transient(false) → Boolean.FALSE；只标注 setter → 同样生效；未标注 → 值属性缺失（null） | testIntrospectorBehavior |
| P1 | getter/setter 同时标注且值冲突 → **JDK-8 以 setter 为准**（实测记录，与 JDK-9+ 相反） | testGetterSetterConflict |
| P1 | 版本差异：`Class.forName("java.beans.BeanProperty"/"java.beans.JavaBean")` → ClassNotFoundException | testVersionDifferences |

### 易错规范细节
- **行为语义最易写错**：常见说法是「@Transient 让属性从内省结果中消失」——实测两个 JDK 均不删除属性，而是 `pd.setValue("transient", Boolean)`；断言必须按值属性写。
- **默认值是 true**（`boolean value() default true;`），反射 `getDefaultValue()` 实证；不要按「注解布尔默认皆 false」的惯性写断言。
- **冲突优先级是版本差异**：JDK-8 setter 的注解覆盖 getter 的；JDK-9+（PropertyInfo 重写）getter 的注解先生效且处理即止。JDK-8 与 JDK-27 双侧断言相反。
- 注解不 @Documented（javadoc 工具不展示），但 RUNTIME retention 保证内省路径可见。
- 样例 Bean 必须 `public static` 嵌套类，否则 Introspector 上下文不可达。
