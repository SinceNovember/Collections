## Set源码分析
### Set继承图:
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/tup.jpg)
## `HashSet`
### 什么是`HashSet`
  HashSet实现Set接口，由哈希表（实际上是一个HashMap实例）支持。它不保证set 的迭代顺序；特别是它不保证该顺序恒久不变。此类允许使用null元素
### `HashSet`特点
  1.存入的顺序与遍历输出的顺序不一定一致。
  2.存入的不允许重复。
  3.实现基于HashMap
### `HashSet`方法
  应为其基本上是调用HashMap，所有其方法都是HashMap中的。
## `LinkedHashSet`
### 什么是`LinkedHashSet`
  LinkedHashSet是hashSet的一个子类,也是用HashCode值来决定元素存储位置,但是LinkedHashSet同时用链表来维护元素的次序(元素的顺序总是与添加的顺序一致),这样看起来元素是以插入的顺序保存的.这样当遍历LinkedHashSet的时候,
LinkedHashSet就会按元素的添加顺序来访问集合里的元素.
### `LinkedHashSet`特点
  1.存入的顺序与遍历输出的顺序一致。
  2.存入的不允许重复。
  3.继承HashSet，底层使用LinkedHashMap
### `LinkedHashSet`方法
  应为其基本上是继承HashSet，但使用的是LinkedHashMap,方法与Set的方法差不多。
