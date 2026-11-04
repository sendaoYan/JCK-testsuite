# java.io.SerializablePermission 测试方案 (DESIGN)

## 1. API 功能
- final BasicPermission 子类；`(String)` 与 `(String, String)` 构造；actions 恒为空（被忽略）。

## 2. Java 用户如何使用
- 安全管理器授权序列化扩展（enableSubclassImplementation / enableSubstitution）。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/SerializablePermission.java`；BasicPermission 名校验。

## 4. 测试用例设计 + 自动化实现（SerializablePermissionTest.java，纯黑盒）
黑盒场景:
1. 两个文档化目标名构造、actions 空。
2. implies 精确同名匹配、不跨类型、非 null。
3. null/空名校验消息。
4. 两参构造忽略 actions。
5. equals/hashCode 契约。
6. "enable*" 不作通配。
7. 序列化往返相等。
- 健壮性: 无反射白盒（JDK 8 无 @Deprecated(forRemoval)）。
