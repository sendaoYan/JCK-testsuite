# javax.management.openmbean.OpenMBeanInfo 设计案

## 1. API 功能
开放 MBean 元信息接口：className/attributes/constructors/operations/notifications。

## 2. Java 用户如何使用
由 OpenMBeanInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 聚合构建+各集合查询。

## 深化（2026-11，批 G）
- 精确方法集 9 个；无公开字段。
- 四段经接口引用；null 数组 → 空；equals 成功/失败两路径；元素 instanceof OpenMBean*Info。
- 版本差异：零差异。
