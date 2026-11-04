# java.beans.beancontext.BeanContextSupport.BCSIterator — 设计案

## 1. API 功能
BCSIterator 嵌套 final 类（implements Iterator）。hasNext/next/remove。

## 2. Java 用户如何使用
BeanContextSupport.iterator() 返回之，用于遍历子 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：final + Iterator。黑盒（`testBlackBox`）：iterator() 返回 BCSIterator，hasNext/next/remove。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 8 → 32。新增：结构（protected static final、唯一包私有 1 参构造器 java.util.Iterator）；空迭代器 next → NoSuchElementException；内容与 toArray 集合相等；hasNext 幂等；两次调用返回独立实例；remove() 在 next 前调用不抛 IllegalStateException（BCSIterator 显式 no-op，偏离 Iterator 契约，记录）；fail-fast：迭代中结构性 add/remove → next 抛 ConcurrentModificationException。
- JDK-27 与 JDK-8 行为完全一致。
