package com.liu.collectionstasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

public class Arraylisttask {
	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		Random random=new Random();
		System.out.println("������n��ֵ:");
		int n=scanner.nextInt();
		ArrayList<Integer> arry=new ArrayList<Integer>(2*n);
		for(int i=0;i<n;i++)
		{
			arry.add(random.nextInt(100));
		}
		System.out.println(arry);
		System.out.println("������k��ֵ");
		int k=scanner.nextInt();
		for(int i=0;i<k;i++)
		{
			int m=arry.get(0);
			arry.remove(0);
			arry.add(m);
		}
		System.out.println("����ɾ���������:");
		System.out.println(arry);
		Collections.sort(arry);
		System.out.println("������list����:");
		Iterator<Integer> it=arry.iterator();
		while(it.hasNext())
		{
			System.out.print(it.next()+" ");
		}
		System.out.println();
		System.out.println("treeset���:");
		TreeSet<Integer> treeSet=new TreeSet<Integer>();
		treeSet.addAll(arry);
		Iterator<Integer> treeit=treeSet.iterator();
		while(treeit.hasNext())
		{
			System.out.print(treeit.next()+" ");
		}
		
	}
}
