# javax.management.openmbean.OpenMBeanOperationInfo 设计案

## 1. API 功能
开放 MBean 操作信息接口：getName/getImpact/getReturnOpenType。

## 2. Java 用户如何使用
由 OpenMBeanOperationInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanOperationInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 构造+impact/return 类型查询。
