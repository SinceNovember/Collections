package com.liu.Set;

import java.util.Collection;
import java.util.Set;

public class LinkedHashSet<E>  
extends HashSet<E>  
implements Set<E>, Cloneable, java.io.Serializable {  //继承与HashSet、又基于LinkedHashMap来实现的。

private static final long serialVersionUID = -2851667679971038690L;  

/** 
 * 构造一个带有指定初始容量和加载因子的新空链接哈希set。 
 * 
 * 底层会调用父类的构造方法，构造一个有指定初始容量和加载因子的LinkedHashMap实例。 true是代表调用HashSet的一个构造函数，那个构造函数true时，调用的是LinkedHashMap
 * @param initialCapacity 初始容量。 
 * @param loadFactor 加载因子。 
 */  
public LinkedHashSet(int initialCapacity, float loadFactor) {  
    super(initialCapacity, loadFactor, true);  
}  

/** 
 * 构造一个带指定初始容量和默认加载因子0.75的新空链接哈希set。 
 * 
 * 底层会调用父类的构造方法，构造一个带指定初始容量和默认加载因子0.75的LinkedHashMap实例。 
 * @param initialCapacity 初始容量。 
 */  
public LinkedHashSet(int initialCapacity) {  
    super(initialCapacity, .75f, true);  
}  

/** 
 * 构造一个带默认初始容量16和加载因子0.75的新空链接哈希set。 
 * 
 * 底层会调用父类的构造方法，构造一个带默认初始容量16和加载因子0.75的LinkedHashMap实例。 
 */  
public LinkedHashSet() {  
    super(16, .75f, true);  
}  

/** 
 * 构造一个与指定collection中的元素相同的新链接哈希set。 
 *  
 * 底层会调用父类的构造方法，构造一个足以包含指定collection 
 * 中所有元素的初始容量和加载因子为0.75的LinkedHashMap实例。 
 * @param c 其中的元素将存放在此set中的collection。 
 */  
public LinkedHashSet(Collection<? extends E> c) {  
    super(Math.max(2*c.size(), 11), .75f, true);  
    addAll(c);  
}  
}  