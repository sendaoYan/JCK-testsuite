# javax.management.MBeanTrustPermission 契约测试设计（2026-11 新建）

## 1. API 功能
- 签名 MBean 注册信任权限（BasicPermission 子类）：target 仅 register/*。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanTrustPermission。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：target/implies/序列化/集合

### 新增覆盖
1. target 仅 "register" 与 "*"；register→register true、register→* false、*→register true；getActions 恒 ""。
2. null name → NPE；未知 target → IAE（"MBeanTrustPermission: Unknown target name [bogus]"）；非 null actions → IAE（null actions 合法）。
3. equals/hashCode/序列化 round-trip。
4. newPermissionCollection 非空，add "*" 后 implies register。

### 参考来源
- Oracle javadoc: MBeanTrustPermission。
