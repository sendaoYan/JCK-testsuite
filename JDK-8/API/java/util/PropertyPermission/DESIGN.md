# java.util.PropertyPermission 测试设计案（JDK-8）

> 覆盖目标：完整契约：动作归一化、implies 矩阵（通配/前缀）、BasicPermission 继承、newPermissionSet 语义。

## 1. API 功能

- 构造器 (String name, String actions)（动作次序无关归一）
- implies(Permission)；getActions（"read,write" 规范序）；继承 java.security.BasicPermission

## 2. 使用方式

new PropertyPermission("java.home", "read") → implies 前缀/通配矩阵断言。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/PropertyPermission`（属性名通配语义）
- JDK 源码 BasicPermission 路径匹配

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 动作归一（"write,read" → "read,write"） | testPermissionContracts |
| P0 | implies 精确名/前缀/*矩阵 | testPermissionContracts |
| P1 | 无动作/未知动作异常路径 | testPropertyPermission |
| P0 | 契约深化：implies 矩阵/动作规范化/通配与前缀/权限集合 | testPermissionContracts |
