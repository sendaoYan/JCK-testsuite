# java.beans.ParameterDescriptor — 设计案

## 1. API 功能
方法参数描述符（extends FeatureDescriptor），无特有方法，仅提供命名。

## 2. Java 用户如何使用
`MethodDescriptor` 的参数数组中每项一个 ParameterDescriptor，用于参数命名。

## 3. 开源代码实际用法调研
JDK 源码 `ParameterDescriptor.java`；`Introspector` 参数描述。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：继承的 name/shortDescription 行为。

## 5. 深化扩展（top-up）
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 公共面：恰 1 个公共构造器（无参）、自有 public 方法 0、声明字段 0（空壳子类） | testWhiteBox |
| P0 | 继承默认态/回落规则/标志往返/值表（含 null 键值 NPE 矩阵）/attributeNames 恰为所设键 | testDefaultState/testFallThroughRules/testFlags/testValues/testValueNegatives/testAttributeNames |
| P1 | 嵌入 MethodDescriptor 的元素身份与名字可见；identity equals；非 Serializable | testInsideMethodDescriptor/testToStringAndEquals/testNotSerializable |

### 易错规范细节（双侧实测一致）
- 源码即空壳：getDeclaredFields()==0、无自有 public 方法（仅默认构造器 + 包私有拷贝构造器）。
- 值表继承 Hashtable 语义（null 键/值/查询 NPE）；非序列化家族成员。
