# java.beans.MethodDescriptor 测试设计案

> 覆盖目标：完整契约（两个构造器、getParameterDescriptors null/防御性拷贝、多种方法形态、
> setName 独立性、无公共 setter、继承行为、identity equals、非序列化）。headless 安全。

## 一、API 功能
- 方法描述符（extends FeatureDescriptor）：`(Method)` 与 `(Method, ParameterDescriptor[])`。
- getMethod（synchronized）返回构造时的 Method 身份；getParameterDescriptors 返回防御性拷贝。
- 无 setMethod/setParameterDescriptors 公共方法（构造后不可变部分）。

## 二、使用方式
- `BeanInfo.getMethodDescriptors()` 返回；ParameterDescriptor 为参数提供名字/描述。
- name/displayName 默认取 Method 名，setName 重命名不影响 getMethod。

## 三、参考来源
- Oracle javadoc Java SE 8（`java/beans/MethodDescriptor.html`）。
- JavaBeans 规范 1.01-A 第 5.5/8.6 节（方法描述与 BeanInfo）。
- OpenJDK `MethodDescriptor.java`（params.clone() 防御性拷贝实现）。

## 四、测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 两构造器 getMethod 身份；1 参构造 getParameterDescriptors **null** | testExistingBasics/testParameterDescriptorsContract |
| P0 | 2 参构造：长度/元素身份/名字可见；改返回数组不影响内部（防御性拷贝）；两次调用返回不同数组 | testParameterDescriptorsContract |
| P1 | 静态方法（Collections.emptyList）、Object.toString、多参方法均可用 | testMethodKinds |
| P1 | name/displayName/shortDescription 默认取方法名；setName 独立 | testMethodKinds/testSetNameIndependence |
| P1 | 继承 FeatureDescriptor：标志/displayName/shortDescription/值表 | testInheritedFeatureBehavior |
| P1 | 无公共 setMethod/setParameterDescriptors（反射断言） | testWhiteBox |
| P1 | equals 未覆写（identity）；toString 含方法名；非 Serializable | testEqualsIdentity/testToString/testNotSerializable |

## 易错规范细节（实测，JDK-8 与 JDK-27 一致）
- javadoc 明确：未提供参数描述时 getParameterDescriptors 返回 null（非空数组）。
- 返回的 ParameterDescriptor[] 是内部数组的 clone：`x != x`（两次调用）、改元素不影响内部。
- MethodDescriptor 不覆写 equals——两个描述同一 Method 的实例互不相等。
- 非序列化家族成员：writeObject 抛 NotSerializableException。
