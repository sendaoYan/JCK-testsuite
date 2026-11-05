# org.omg.CosNaming.BindingIteratorHelper

## API 功能
BindingIterator（绑定迭代器）对象引用 Helper。
## 使用方式
list() 第二出参 narrow 后 next_one/next_n 迭代。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id `IDL:omg.org/CosNaming/BindingIterator:1.0`、tk_objref；nil 三通道；narrow(null)→null；负例 MARSHAL。
