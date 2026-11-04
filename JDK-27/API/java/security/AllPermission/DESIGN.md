# java.security.AllPermission 黑盒行为测试设计

## 1. API 功能
- 构造：`AllPermission()`、`AllPermission(String, String)`（名字/动作被忽略）
- 查询：`getActions()` 恒为 `<all permissions>`、`implies` 恒真、`newPermissionCollection()`

## 2. Java 用户如何使用
策略文件里 `grant { permission java.security.AllPermission; }`；代码里用于测试环境全开。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AllPermission.java`（AllPermissionCollection 只接受 AllPermission）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/reflect/Proxy/Basic1.java`（保护域授予 AllPermission 的场景）

## 4. 测试用例设计 + 自动化实现
- 正常：两构造器等价、implies 任意权限为真、hashCode/equals 自反
- 组合：AllPermissionCollection.add(非 AllPermission) 抛 IAE；包含 AllPermission 的集合 implies 一切
- 异常：null implies 目标 → false

断言入口：`AllPermissionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
