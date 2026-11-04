# java.beans.MethodDescriptor — 设计案

## 1. API 功能
方法描述符（extends FeatureDescriptor）。构造器 `(method)` / `(method, parameterDescriptors)`；getMethod/getParameterDescriptors。

## 2. Java 用户如何使用
`BeanInfo.getMethodDescriptors()` 返回公开方法列表及其参数描述。

## 3. 开源代码实际用法调研
JDK 源码 `MethodDescriptor.java`；`Introspector` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：两个构造器的 method 与 parameterDescriptors。

## 5. 深化扩展（top-up）
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 1 参构造 getParameterDescriptors **null**；2 参：长度/元素身份/防御性拷贝（改返回数组不影响内部、两次调用不同数组）；空数组保留 | testParameterDescriptorsContract |
| P1 | 静态方法/Object.toString/多参方法；name/displayName/shortDescription 默认取方法名 | testMethodKinds |
| P1 | setName 独立于 getMethod；继承标志/描述/值表 | testSetNameIndependence/testInheritedFeatureBehavior |
| P1 | 无公共 setMethod/setParameterDescriptors（反射）；identity equals；非 Serializable | testWhiteBox/testEqualsIdentity/testNotSerializable |

### 易错规范细节（双侧实测一致）
- 返回的 ParameterDescriptor[] 是 clone：两次调用 `!=`、改元素不影响内部。
- MD 不覆写 equals；非序列化家族成员。
