package com.liu.Set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeMap;

public class TreeSet<E> extends AbstractSet<E>
implements NavigableSet<E>, Cloneable, java.io.Serializable
{
	 private transient NavigableMap<E,Object> m;// ʹ��NavigableMap�����key������Set���ϵ�Ԫ��
	 private static final Object PRESENT = new Object();//PRESENT�ᱻ����Map��value��key�����ɼ�ֵ��
	  TreeSet(NavigableMap<E,Object> m) {// ��TreeMap��ֵ�� "NavigableMap����m"
	        this.m = m;
	    }
	  // ���������Ĺ��캯��������һ���յ�TreeMap
	    //����Ȼ���򷽷�����һ���µ�TreeMap���ٸ��ݸ�TreeMap����һ��TreeSet
	    //ʹ�ø�TreeMap��key������Set���ϵ�Ԫ��
	  public TreeSet() {
	        this(new TreeMap<E,Object>());
	    }
	//�Զ�������ķ�ʽ����һ���µ�TreeMap�����ݸ�TreeMap����һ��TreeSet
	    //ʹ�ø�TreeMap��key������set���ϵ�Ԫ��
	  public TreeSet(Comparator<? super E> comparator) {
	        this(new TreeMap<>(comparator));
	    }
	  // ����TreeSet����������c�е�ȫ��Ԫ�ض���ӵ�TreeSet��
	  public TreeSet(Collection<? extends E> c) {
	        this();
	        addAll(c);  // ������c�е�Ԫ��ȫ����ӵ�TreeSet��
	    }
	  // ����TreeSet������s�е�ȫ��Ԫ�ض���ӵ�TreeSet��
	  public TreeSet(SortedSet<E> s) {
	        this(s.comparator());
	        addAll(s);
	    }
	// ����TreeSet��˳�����еĵ�������
	    // ��ΪTreeSetʱTreeMapʵ�ֵģ���������ʵ����ʱ����TreeMap�ġ���������Ӧ�ĵ�����
	  public Iterator<E> iterator() {
	        return m.navigableKeySet().iterator();
	    }
	  // ����TreeSet���������еĵ�������
	    // ��ΪTreeSetʱTreeMapʵ�ֵģ���������ʵ����ʱ����TreeMap�ġ���������Ӧ�ĵ�����
	  public Iterator<E> descendingIterator() {
	        return m.descendingKeySet().iterator();
	    }
	  //����һ�������Setʵ����ͨ�������TreeMapȻ��ת���TreeSet
	  public NavigableSet<E> descendingSet() {
	        return new TreeSet<>(m.descendingMap());
	    }
	// ����TreeSet�Ĵ�С
	  public int size() {
	        return m.size();
	    }
	  // ����TreeSet�Ƿ�Ϊ��
	  public boolean isEmpty() {
	        return m.isEmpty();
	    }
	// ����TreeSet�Ƿ��������(o)
	  public boolean contains(Object o) {
	        return m.containsKey(o);
	    }
	  // ���e��TreeSet��
	  public boolean add(E e) {
	        return m.put(e, PRESENT)==null;
	    }
	// ɾ��TreeSet�еĶ���o
	  public boolean remove(Object o) {
	        return m.remove(o)==PRESENT;
	    }
	// ���TreeSet
	  public void clear() {
	        m.clear();
	    }
	  // ������c�е�ȫ��Ԫ����ӵ�TreeSet��
	  public  boolean addAll(Collection<? extends E> c) {
	        // Use linear-time version if applicable
	        if (m.size()==0 && c.size() > 0 &&
	            c instanceof SortedSet &&
	            m instanceof TreeMap) {
	        	//��C����ǿ��ת��ΪSortedSet����
	            SortedSet<? extends E> set = (SortedSet<? extends E>) c;
	            //��m����ǿ��ת��ΪTreeMap����
	            TreeMap<E,Object> map = (TreeMap<E, Object>) m;
	            Comparator<?> cc = set.comparator();
	            Comparator<? super E> mc = map.comparator();
	          //���cc��mc����Comparator���
	            if (cc==mc || (cc != null && cc.equals(mc))) {
	            	//��Collection������Ԫ����ӳ�TreeMap���ϵ�key
	                map.addAllForTreeSet(set, PRESENT);
	                return true;
	            }
	        }
	        return super.addAll(c);
	    }
	  // ������Set��ʵ������ͨ��TreeMap��subMap()ʵ�ֵġ�
	  public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
              E toElement,   boolean toInclusive) {
		  		 return new TreeSet<>(m.subMap(fromElement, fromInclusive,
                   toElement,   toInclusive));
	  }
	  // ����Set��ͷ������Χ�ǣ���ͷ����toElement��
	    // inclusive���Ƿ����toElement�ı�־
	  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
	        return new TreeSet<>(m.headMap(toElement, inclusive));
	    }

	    // ����Set��β������Χ�ǣ���fromElement����β��
	    // inclusive���Ƿ����fromElement�ı�־
	  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
	        return new TreeSet<>(m.tailMap(fromElement, inclusive));
	    }
	  // ������Set����Χ�ǣ���fromElement(����)��toElement(������)��
	  public SortedSet<E> subSet(E fromElement, E toElement) {
	        return subSet(fromElement, true, toElement, false);
	    }
	// ����Set��ͷ������Χ�ǣ���ͷ����toElement(������)
	  public SortedSet<E> headSet(E toElement) {
	        return headSet(toElement, false);
	    }
	  // ����Set��β������Χ�ǣ���fromElement����β(������)��
	  public SortedSet<E> tailSet(E fromElement) {
	        return tailSet(fromElement, true);
	    }
	  // ����Set�ıȽ���
	    public Comparator<? super E> comparator() {
	        return m.comparator();
	    }
	 // ����Set�ĵ�һ��Ԫ��
	    public E first() {
	        return m.firstKey();
	    }
	    // ����Set�����һ��Ԫ��
	    public E last() {
	        return m.lastKey();
	    }
	    // ����Set��С��e�����Ԫ��
	    public E lower(E e) {
	        return m.lowerKey(e);
	    }
	 // ����Set�д���/����e����СԪ��
	    public E floor(E e) {
	        return m.floorKey(e);
	    }
	    // ����Set�д���/����e����СԪ��
	    public E ceiling(E e) {
	        return m.ceilingKey(e);
	    }
	 //   ����Set�д���e����СԪ��
	    public E higher(E e) {
	        return m.higherKey(e);
	    }
	    // ��ȡ��һ��Ԫ�أ�������Ԫ�ش�TreeMap��ɾ����
	    public E pollFirst() {
	        Map.Entry<E,?> e = m.pollFirstEntry();
	        return (e == null) ? null : e.getKey();
	    }
	    // ��ȡ���һ��Ԫ�أ�������Ԫ�ش�TreeMap��ɾ����
	    public E pollLast() {
	        Map.Entry<E,?> e = m.pollLastEntry();
	        return (e == null) ? null : e.getKey();
	    }
	    // ��¡һ��TreeSet��������Object����
	    @SuppressWarnings("unchecked")
	    public Object clone() {
	        TreeSet<E> clone;
	        try {
	            clone = (TreeSet<E>) super.clone();
	        } catch (CloneNotSupportedException e) {
	            throw new InternalError(e);
	        }

	        clone.m = new TreeMap<>(m);
	        return clone;
	    }
	 // java.io.Serializable��д�뺯��
	    // ��TreeSet�ġ��Ƚ��������������е�Ԫ��ֵ����д�뵽�������
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws java.io.IOException {

	            s.defaultWriteObject();

	         // д��Ƚ���
	            s.writeObject(m.comparator());

	            // д������
	            s.writeInt(m.size());

	         // д�롰TreeSet�е�ÿһ��Ԫ�ء�
	            for (E e : m.keySet())
	                s.writeObject(e);
	        }
	    // java.io.Serializable�Ķ�ȡ����������д�뷽ʽ����
	    // �Ƚ�TreeSet�ġ��Ƚ��������������е�Ԫ��ֵ�����ζ���
	    private void readObject(java.io.ObjectInputStream s)
	            throws java.io.IOException, ClassNotFoundException {
	            // Read in any hidden stuff
	            s.defaultReadObject();

	            // ���������ж�ȡTreeSet�ġ��Ƚ�����
	            @SuppressWarnings("unchecked")
	                Comparator<? super E> c = (Comparator<? super E>) s.readObject();

	            // Create backing TreeMap
	            TreeMap<E,Object> tm = new TreeMap<>(c);
	            m = tm;

	            // ���������ж�ȡTreeSet�ġ�������
	            int size = s.readInt();
	         // ���������ж�ȡTreeSet�ġ�ȫ��Ԫ�ء�
	            tm.readTreeSet(size, s, PRESENT);
	        }
	    public Spliterator<E> spliterator() {
	        return TreeMap.keySpliteratorFor(m);
	    }

	    private static final long serialVersionUID = -2479143000061671589L;

}
