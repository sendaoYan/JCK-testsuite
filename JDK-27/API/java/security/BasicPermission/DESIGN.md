# BasicPermission — 设计案

## 1. API 功能
基础权限抽象类（extends Permission）。name/name+actions 构造器 + getName/getActions(恒 "") + implies + newPermissionCollection。

## 2. 使用方式
子类 `class BP extends BasicPermission { BP(String n){ super(n); } }`。

## 3. 参考来源
- javadoc `java.security.BasicPermission`。
- JCK runtime-8b `api/java_security/BasicPermission/`。

## 4. 测试用例设计 + 易错规范细节
- 抽象类层级；构造器 + getName/getActions。
- null 名→NPE、空名→IAE。
- implies：精确匹配、通配 "a.*" 多级（含 a.b.c）、"*" 全匹配、精确不匹配更深的兄弟。
- equals/hashCode：同子类同名相等、不同子类不相等、非 Permission 不相等。
- newPermissionCollection：空 implies false、add 后 true。
- 易错点：BasicPermission.getActions 恒为 ""（忽略 actions）；"a.*" 匹配多级前缀。
