# javax.print.attribute.HashPrintServiceAttributeSet 黑盒行为测试设计

## 1. API 功能
HashPrintServiceAttributeSet 是 PrintServiceAttributeSet 的哈希实现，extends HashAttributeSet，限制成员必须实现 PrintServiceAttributeSet 接口。

## 2. Java 用户如何使用
new HashPrintServiceAttributeSet() 后 add 属性；错误 category 抛 ClassCastException。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/。

## 4. 测试用例设计 + 自动化实现
黑盒：空集、增删查、重复 add、toArray、clear、equals/hashCode、错误 category ClassCastException、数组构造器。白盒：public、继承/实现关系。

