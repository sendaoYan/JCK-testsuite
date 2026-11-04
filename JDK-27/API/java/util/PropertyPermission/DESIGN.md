# java.util.PropertyPermission — 测试设计案

## 1. API 功能
系统属性读/写的权限对象（java.security.BasicPermission 子类，deprecated for removal）。构造器 (String name, String actions)，getActions()/implies()/equals()/hashCode()/newPermissionCollection()。

## 2. Java 用户如何使用
SecurityManager 场景：`new PropertyPermission("java.*", "read")` 判断能否读某属性；
actions 用逗号分隔的 read/write。链路：构造 -> implies 判定 -> getActions。

## 3. 开源代码实际用法调研
- OpenJDK `PropertyPermission`（本地 `~/git/jdk25u-dev`）：actions 归一化为 `read,write` 排序；
  `java.*` 通配 implies `java.home`；非法 action 抛 IllegalArgumentException——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/security/PropertyPermission/`.

## 4. 测试用例设计 + 自动化实现（PropertyPermissionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| getActions | read,write 归一化排序 |
| implies | 通配 implies 具体名、动作子集判定 |
| 非法 action | 抛 IllegalArgumentException |
| equals/hashCode | 同名同 action 相等 |

共 4 组场景、约 8 个 check（`testBlackBox()`）。
