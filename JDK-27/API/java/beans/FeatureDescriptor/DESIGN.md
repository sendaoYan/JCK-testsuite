# java.beans.FeatureDescriptor — 设计案

## 1. API 功能
Bean 特性（属性/事件/方法）描述符基类。name/displayName/shortDescription/expert/hidden/preferred 及通用属性表 setValue/getValue/getAttributeNames。

## 2. Java 用户如何使用
`BeanDescriptor`/`PropertyDescriptor` 等继承；工具通过 getName/getShortDescription/isExpert 展示 Bean 特性。

## 3. 开源代码实际用法调研
JDK 源码 `FeatureDescriptor.java`；所有描述符类的共同基类。

## 4. 测试用例设计 + 自动化实现
白盒：非接口。黑盒（`testBlackBox`）：name/displayName/shortDescription/expert/hidden/preferred 及属性表 set/get/enumerate。

## 5. 深化扩展（top-up）
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认态：name/displayName/shortDescription null、三标志 false、attributeNames 空枚举 | testDefaultState |
| P0 | 回落规则：setName→displayName/shortDescription 跟随；逐级覆盖；setShortDescription(null) 回落到 displayName | testDisplayNameFallThrough |
| P0 | 值表：String/Integer/Boolean、覆盖写、未知键 null、空串键合法；null 键/null 值/null 查询 → NPE（Hashtable 语义） | testValues/testValueNegatives |
| P1 | setName(null) 合法；attributeNames 恰为所设键集（按 set 比较）；toString 含 name 与 `k=v` | testSetNameNull/testAttributeNames/testToString |
| P1 | identity equals（无 equals 覆写）；**非 Serializable**（isAssignableFrom false + NotSerializableException） | testEqualsIdentity/testNotSerializable |
| P0 | 白盒：public 声明方法恰 16 个、无 equals 覆写、直接继承 Object | testWhiteBox |

### 易错规范细节（双侧实测一致）
- **FeatureDescriptor 家族（FD/BD/MD/ParamD/PD/IPD/ESD）全部不可序列化**：javadoc 未声明
  Serializable、ObjectStreamClass.lookup 返回 null、writeObject 抛 NotSerializableException。
- setValue 值不可为 null（Hashtable.put）；attributeNames 为散列序，须按集合比较。
- JDK-27 NPE 带 helpful message（"Cannot invoke ... because key is null"），JDK-8 message 为 null；
  只断言异常类，不断言 message。
