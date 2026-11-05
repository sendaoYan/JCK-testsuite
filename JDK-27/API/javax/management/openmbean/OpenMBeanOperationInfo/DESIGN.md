# javax.management.openmbean.OpenMBeanOperationInfo 设计案

## 1. API 功能
开放 MBean 操作信息接口：getName/getImpact/getReturnOpenType。

## 2. Java 用户如何使用
由 OpenMBeanOperationInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanOperationInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 构造+impact/return 类型查询。

## 深化（2026-11，批 G）
- 精确方法集 9 个；无公开字段。
- getReturnOpenType 经接口引用（SimpleType 单例）；SimpleType.VOID 无返回；impact 四常量 0..3
  经接口引用与 MBeanOperationInfo 常量一致；equals 不含 description。
- 版本差异：零差异。
