# javax.print.attribute.HashAttributeSet 黑盒行为测试设计

## 1. API 功能
HashAttributeSet 是 AttributeSet 的哈希实现，按 category 存取、同 category 替换。构造器 (Attribute)/(Attribute[])/(AttributeSet)。

## 2. Java 用户如何使用
new HashAttributeSet() 后 add 属性；get(category) 检索；equals 与插入顺序无关。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/HashAttributeSet.java。

## 4. 测试用例设计 + 自动化实现
黑盒：增删查、category 替换、containsKey/Value、toArray、equals/hashCode 与顺序无关、四种构造器。白盒：public、implements AttributeSet、Serializable。

