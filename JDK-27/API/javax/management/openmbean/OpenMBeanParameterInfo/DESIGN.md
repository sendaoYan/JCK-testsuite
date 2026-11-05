# javax.management.openmbean.OpenMBeanParameterInfo 设计案

## 1. API 功能
开放 MBean 参数信息接口：default/legal/min/max + isValue。

## 2. Java 用户如何使用
由 OpenMBeanParameterInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanParameterInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 构造默认值+isValue 判断。

## 深化（2026-11，批 G）
- 精确方法集匹配（15 个 name/参数个数，集合相等），无公开字段，与 OpenMBeanAttributeInfo 平级。
- 15 个方法全部经接口引用在 Support 实例上断言真值；min/max、legal、plain 三形态。
- 易错：有 defaultValue 时 isValue(null)==true（null 表示用默认值）；plain 形态 isValue(null)==false。
- 版本差异：JDK-8 与 JDK-27 零差异。
