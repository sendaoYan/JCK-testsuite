# AttributeListImpl — 设计案

## 1. API 功能
SAX1 AttributeList 可变实现：add/remove/clear + 名字/类型/值访问。

## 2. Java 用户如何使用
SAX1 时代构造属性表；现属遗留。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：add 累加、按名按索引三访问器；缺名 null；越界 getName/getType/getValue 全 null（不抛）；removeAttribute(String 名字而非索引！)；拷贝构造独立快照；clear 只清自己。易错：remove 收 String。
