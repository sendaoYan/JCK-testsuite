# javax.management.openmbean.OpenMBeanParameterInfo 设计案

## 1. API 功能
开放 MBean 参数信息接口：default/legal/min/max + isValue。

## 2. Java 用户如何使用
由 OpenMBeanParameterInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanParameterInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 构造默认值+isValue 判断。
