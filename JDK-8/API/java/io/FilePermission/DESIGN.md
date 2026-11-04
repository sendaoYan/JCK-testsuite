# java.io.FilePermission 测试方案 (DESIGN)

## 1. API 功能
- final Permission 子类；`(String path, String actions)` 构造；action 规范化为 read,write,execute,delete。

## 2. Java 用户如何使用
- 安全管理器按路径与动作集授权文件访问；支持 "/-" 递归与 "/*" 平铺与 "<<ALL FILES>>"。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FilePermission.java`。

## 4. 测试用例设计 + 自动化实现（FilePermissionTest.java，纯黑盒）
黑盒场景:
1. action 规范顺序 + 重复折叠。
2. 非法/空 action 拒绝（消息 "invalid permission: ..."）。
3. "/-" 递归 implies 深路径、非兄弟、非目录自身、动作子集。
4. "/*" 仅平铺直接子项。
5. "<<ALL FILES>>" implies 任意路径。
6. 精确文件与动作超集/子集 implies。
7. 非相关类型与 null 不 implies。
8. equals/hashCode 按名+掩码。
9. 名字中孤立 "*" 作字面量。
10. newPermissionCollection 可用。
11. 序列化往返相等。
- 健壮性: 无反射白盒（JDK 8 无 @Deprecated(forRemoval)）。
