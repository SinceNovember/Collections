package com.liu.Set;

import java.util.Collection;
import java.util.Set;

public class LinkedHashSet<E>  
extends HashSet<E>  
implements Set<E>, Cloneable, java.io.Serializable {  //�̳���HashSet���ֻ���LinkedHashMap��ʵ�ֵġ�

private static final long serialVersionUID = -2851667679971038690L;  

/** 
 * ����һ������ָ����ʼ�����ͼ������ӵ��¿����ӹ�ϣset�� 
 * 
 * �ײ����ø���Ĺ��췽��������һ����ָ����ʼ�����ͼ������ӵ�LinkedHashMapʵ���� true�Ǵ������HashSet��һ�����캯�����Ǹ����캯��trueʱ�����õ���LinkedHashMap
 * @param initialCapacity ��ʼ������ 
 * @param loadFactor �������ӡ� 
 */  
public LinkedHashSet(int initialCapacity, float loadFactor) {  
    super(initialCapacity, loadFactor, true);  
}  

/** 
 * ����һ����ָ����ʼ������Ĭ�ϼ�������0.75���¿����ӹ�ϣset�� 
 * 
 * �ײ����ø���Ĺ��췽��������һ����ָ����ʼ������Ĭ�ϼ�������0.75��LinkedHashMapʵ���� 
 * @param initialCapacity ��ʼ������ 
 */  
public LinkedHashSet(int initialCapacity) {  
    super(initialCapacity, .75f, true);  
}  

/** 
 * ����һ����Ĭ�ϳ�ʼ����16�ͼ�������0.75���¿����ӹ�ϣset�� 
 * 
 * �ײ����ø���Ĺ��췽��������һ����Ĭ�ϳ�ʼ����16�ͼ�������0.75��LinkedHashMapʵ���� 
 */  
public LinkedHashSet() {  
    super(16, .75f, true);  
}  

/** 
 * ����һ����ָ��collection�е�Ԫ����ͬ�������ӹ�ϣset�� 
 *  
 * �ײ����ø���Ĺ��췽��������һ�����԰���ָ��collection 
 * ������Ԫ�صĳ�ʼ�����ͼ�������Ϊ0.75��LinkedHashMapʵ���� 
 * @param c ���е�Ԫ�ؽ�����ڴ�set�е�collection�� 
 */  
public LinkedHashSet(Collection<? extends E> c) {  
    super(Math.max(2*c.size(), 11), .75f, true);  
    addAll(c);  
}  
}  