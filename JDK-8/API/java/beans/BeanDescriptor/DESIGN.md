# java.beans.BeanDescriptor 测试设计案

> 覆盖目标：完整契约（两个构造器、null beanClass NPE、name 推导规则、继承行为、
> identity equals、非序列化、SimpleBeanInfo 集成）。headless 安全。

## 一、API 功能
- Bean 全局描述符（extends FeatureDescriptor）：`(beanClass)` 与 `(beanClass, customizerClass)`。
- getBeanClass/getCustomizerClass（1 参构造时 customizer 为 null）。
- 构造时 name 自动取 beanClass 名去掉包前缀。

## 二、使用方式
- `BeanInfo.getBeanDescriptor()` 返回；自定义 SimpleBeanInfo 子类可直接返回手工构造的
  BeanDescriptor（无需 Introspector 搜索路径）。

## 三、参考来源
- Oracle javadoc Java SE 8（`java/beans/BeanDescriptor.html`）。
- JavaBeans 规范 1.01-A 第 8 节（Customizer）与第 5 节（bean descriptor）。
- OpenJDK `BeanDescriptor.java` 构造器源码（name 截取规则：`while (name.indexOf('.') >= 0) substring`）。

## 四、测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 两构造器 beanClass/customizerClass 身份；显式 null customizer 合法 | testExistingBasics/testCustomizerForms |
| P0 | null beanClass → NullPointerException（两个构造器都是） | testNullBeanClass |
| P0 | name 推导：String→"String"、Date→"Date"、嵌套类保留 `$`、Map.Entry→"Map$Entry"、数组类→"String;"（实测形态） | testNameDerivation |
| P1 | displayName 默认=name；setDisplayName 覆盖；shortDescription 跟随 displayName；setName 覆盖派生名 | testDisplayNameDefault |
| P1 | 继承 FeatureDescriptor：三标志、setValue/getValue | testInheritedFeatureBehavior |
| P1 | toString 非空且含 bean 类名 | testToString |
| P1 | equals 未覆写（identity）；非 Serializable → NotSerializableException | testEqualsIdentity/testNotSerializable |
| P1 | SimpleBeanInfo 子类直接读自定义 BeanDescriptor：beanClass/customizerClass/实例稳定 | testSimpleBeanInfoIntegration |
| P0 | 白盒：恰 2 个公共构造器、getBeanClass/getCustomizerClass 存在、无 equals 覆写 | testWhiteBox |

## 易错规范细节（实测，JDK-8 与 JDK-27 一致）
- name 截取只去掉 `.` 包前缀，**不处理 `$`**：嵌套类得到 `BeanDescriptorTest$MyBean`；
  数组类 `java.lang.String[]`（二进制名 `[Ljava.lang.String;`）被截成 `String;`——记录实测行为。
- customizerClass 参数允许显式 null（WeakReference 封装，getCustomizerClass 返回 null）。
- BD 及整个 FeatureDescriptor 家族都不可序列化（断言 NotSerializableException）。
