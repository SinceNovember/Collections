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
 * @date: 2017��12��4�� ����3:52:13 
 * @describe:HashSet�ײ�����HashMapʵ�֣�������Ĺ��ܶ���ͨ������Map�Ĺ��ܡ�
 */
public class HashSet<E>
extends AbstractSet<E>
implements Set<E>, Cloneable, java.io.Serializable//HashSet�̳�AbstractSet�࣬ʵ��Set��Cloneable��Serializable�ӿڡ�����AbstractSet�ṩ Set �ӿڵĹǸ�ʵ�֣��Ӷ�����޶ȵؼ�����ʵ�ִ˽ӿ�����Ĺ�����Set�ӿ���һ�ֲ������ظ�Ԫ�ص�Collection
{                   							  //��ά�����Լ����ڲ����������������û���κ����塣
	static final long serialVersionUID = -5024744406713321676L;
	//����HashMapʵ�֣��ײ�ʹ��HashMap��������Ԫ��
    private transient HashMap<E,Object> map;
    //����һ��Object������ΪHashMap��value  
    private static final Object PRESENT = new Object();
 
    //��ʼ��һ���յ�HashMap����ʹ��Ĭ�ϳ�ʼ����Ϊ16�ͼ�������0.75�� ]b
    public HashSet() {
        map = new HashMap<>();
    }
    //����һ������ָ�� collection �е�Ԫ�ص��� set��
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }
    //����һ���µĿ� set����ײ� HashMap ʵ������ָ���ĳ�ʼ������ָ���ļ������� 
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }
    // ����һ���µĿ� set����ײ� HashMap ʵ������ָ���ĳ�ʼ������Ĭ�ϵļ������ӣ�0.75���� 
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }
    //��ָ����initialCapacity��loadFactor����һ���µĿ����ӹ�ϣ���ϡ�  dummy Ϊ��ʶ �ù��캯����Ҫ�����Ƕ�LinkedHashSet��һ��֧������ 
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
    // iterator()�������ضԴ� set ��Ԫ�ؽ��е����ĵ�����������Ԫ�ص�˳�򲢲����ض��ġ��ײ����HashMap��keySet�������е�key����㷴Ӧ��HashSet�е�����Ԫ�ض��Ǳ�����HashMap��key�У�value����ʹ�õ�PRESENT���󣬸ö���Ϊstatic final��
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }
    //size()���ش� set �е�Ԫ�ص�������set �����������ײ����HashMap��size����������HashMap�����Ĵ�С��
    public int size() {
        return map.size();
    }
    //isEmpty()���ж�HashSet()�����Ƿ�Ϊ�գ�Ϊ�շ��� true�����򷵻�false��
    public boolean isEmpty() {
        return map.isEmpty();
    }
    //�ж�ĳ��Ԫ���Ƿ������HashSet()�У����ڷ���true�����򷵻�false������ȷ�еĽ�Ӧ����Ҫ�������ֹ�ϵ���ܷ���true��(o==null ? e==null : o.equals(e))���ײ����containsKey�ж�HashMap��keyֵ�Ƿ�Ϊ�ա�
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    // ���Ԫ�أ�ʵ����HashMap�����ʵ�壬keyΪ����ģ�ֵΪPRESENT����                     
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
    //�Ƴ�ָ����ֵ��Map�е��Ƴ�
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }
    //���set�������Map�е�
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
