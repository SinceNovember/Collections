package com.liu.Set;

import java.io.InvalidObjectException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Spliterator;

 /** 
 * @ClassName: HashSet 
 * @author: lyd
 * @date: 2017年12月4日 下午3:52:13 
 * @describe:HashSet底层用了HashMap实现，其基本的功能都市通过调用Map的功能。
 */
public class HashSet<E>
extends AbstractSet<E>
implements Set<E>, Cloneable, java.io.Serializable//HashSet继承AbstractSet类，实现Set、Cloneable、Serializable接口。其中AbstractSet提供 Set 接口的骨干实现，从而最大限度地减少了实现此接口所需的工作。Set接口是一种不包括重复元素的Collection
{                   							  //它维持它自己的内部排序，所以随机访问没有任何意义。
	static final long serialVersionUID = -5024744406713321676L;
	//基于HashMap实现，底层使用HashMap保存所有元素
    private transient HashMap<E,Object> map;
    //定义一个Object对象作为HashMap的value  
    private static final Object PRESENT = new Object();
 
    //初始化一个空的HashMap，并使用默认初始容量为16和加载因子0.75。 ]b
    public HashSet() {
        map = new HashMap<>();
    }
    //构造一个包含指定 collection 中的元素的新 set。
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }
    //构造一个新的空 set，其底层 HashMap 实例具有指定的初始容量和指定的加载因子 
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }
    // 构造一个新的空 set，其底层 HashMap 实例具有指定的初始容量和默认的加载因子（0.75）。 
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }
    //以指定的initialCapacity和loadFactor构造一个新的空链接哈希集合。  dummy 为标识 该构造函数主要作用是对LinkedHashSet起到一个支持作用 
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
    // iterator()方法返回对此 set 中元素进行迭代的迭代器。返回元素的顺序并不是特定的。底层调用HashMap的keySet返回所有的key，这点反应了HashSet中的所有元素都是保存在HashMap的key中，value则是使用的PRESENT对象，该对象为static final。
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }
    //size()返回此 set 中的元素的数量（set 的容量）。底层调用HashMap的size方法，返回HashMap容器的大小。
    public int size() {
        return map.size();
    }
    //isEmpty()，判断HashSet()集合是否为空，为空返回 true，否则返回false。
    public boolean isEmpty() {
        return map.isEmpty();
    }
    //判断某个元素是否存在于HashSet()中，存在返回true，否则返回false。更加确切的讲应该是要满足这种关系才能返回true：(o==null ? e==null : o.equals(e))。底层调用containsKey判断HashMap的key值是否为空。
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    // 添加元素，实际在HashMap中添加实体，key为输入的，值为PRESENT对象，                     
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
    //移除指定的值，Map中的移除
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }
    //清空set，即清空Map中的
    public void clear() {
        map.clear();
    }
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
            // Write out any hidden serialization magic
            s.defaultWriteObject();

            // Write out HashMap capacity and load factor
            s.writeInt(map.capacity());
            s.writeFloat(map.loadFactor());

            // Write out size
            s.writeInt(map.size());

            // Write out all elements in the proper order.
            for (E e : map.keySet())
                s.writeObject(e);
        }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            // Read in any hidden serialization magic
            s.defaultReadObject();

            // Read capacity and verify non-negative.
            int capacity = s.readInt();
            if (capacity < 0) {
                throw new InvalidObjectException("Illegal capacity: " +
                                                 capacity);
            }

            // Read load factor and verify positive and non NaN.
            float loadFactor = s.readFloat();
            if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
                throw new InvalidObjectException("Illegal load factor: " +
                                                 loadFactor);
            }

            // Read size and verify non-negative.
            int size = s.readInt();
            if (size < 0) {
                throw new InvalidObjectException("Illegal size: " +
                                                 size);
            }

            // Set the capacity according to the size and load factor ensuring that
            // the HashMap is at least 25% full but clamping to maximum capacity.
            capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                    HashMap.MAXIMUM_CAPACITY);

            // Create backing HashMap
            map = (((HashSet<?>)this) instanceof LinkedHashSet ?
                   new LinkedHashMap<E,Object>(capacity, loadFactor) :
                   new HashMap<E,Object>(capacity, loadFactor));

            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                @SuppressWarnings("unchecked")
                    E e = (E) s.readObject();
                map.put(e, PRESENT);
            }
            public Spliterator<E> spliterator() {
                return new HashMap.KeySpliterator<E,Object>(map, 0, -1, 0, 0);
            }
        }
}
