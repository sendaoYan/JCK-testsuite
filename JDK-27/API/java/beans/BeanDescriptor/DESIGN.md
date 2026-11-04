# java.beans.BeanDescriptor — 设计案

## 1. API 功能
Bean 描述符（extends FeatureDescriptor）。构造器 `(beanClass)` / `(beanClass, customizerClass)`；getBeanClass/getCustomizerClass。

## 2. Java 用户如何使用
`BeanInfo.getBeanDescriptor()` 返回 Bean 的类及定制器（Customizer）。

## 3. 开源代码实际用法调研
JDK 源码 `BeanDescriptor.java`；`SimpleBeanInfo`/工具生成 BeanInfo。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：两个构造器的 beanClass/customizerClass 断言。

## 5. 深化扩展（top-up）
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | null beanClass → NPE（两构造器）；显式 null customizer 合法 | testNullBeanClass/testCustomizerForms |
| P0 | name 推导实测：String→"String"、Date→"Date"、嵌套类保留 `$`（`BeanDescriptorTest$MyBean`）、Map.Entry→"Map$Entry"、数组类→`String;` | testNameDerivation |
| P1 | displayName 默认=name、shortDescription 跟随 displayName、setName 覆盖派生名 | testDisplayNameDefault |
| P1 | 继承三标志/值表；toString 含 bean 类名；identity equals；非 Serializable | testInheritedFeatureBehavior/testToString/testEqualsIdentity/testNotSerializable |
| P1 | SimpleBeanInfo 子类直接返回自定义 BD：beanClass/customizerClass/实例稳定 | testSimpleBeanInfoIntegration |
| P0 | 白盒：恰 2 个公共构造器、无 equals 覆写 | testWhiteBox |

### 易错规范细节（双侧实测一致）
- name 截取只去 `.` 包前缀不处理 `$`；数组二进制名 `[Ljava.lang.String;` 截成 `String;`。
- BD 不可序列化（NotSerializableException）。
