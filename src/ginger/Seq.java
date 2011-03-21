package ginger;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Seq<T> {
	
	private LinkedList<T> objects;

	public Seq(){
		this.objects = new LinkedList<T>();
	}
	
	public Seq(Collection<T> objects){
		this.objects = new LinkedList<T>(objects);
	}
	
	public static <T> Seq<T> s(Collection<T> objects) {
		return new Seq<T>(objects);
	}
	
	public static <T> Seq<T> s(T... objects) {
		return s(asList(objects));
	}

	public Seq<T> add(T object) {
		objects.add(object);
		return this;
	}
	
	public Seq<T> addAll(T... parameters) {
		objects.addAll(asList(parameters));
		return this;
	}

	public boolean isEmpty() {
		return objects.isEmpty();
	}
	
	public int size() {
		return objects.size();
	}

	public List<T> toList() {
		return new LinkedList<T>(objects);
	}
	
	public String join(String separator) {
		StringBuilder result = new StringBuilder();
		
		Iterator<T> iterator = objects.iterator();
		if (iterator.hasNext()) result.append(String.valueOf(iterator.next()));
		
		while (iterator.hasNext()) {
			result.append(separator);
			result.append(String.valueOf(iterator.next()));
		}
		
		return result.toString();
	}

	public Seq<T> removeNullsAndEmpties() {
		LinkedList<T> result = new LinkedList<T>();
		for (T element : objects) {
			if (element == null) continue;
			if (element.toString().trim().equals("")) continue;
			
			result.add(element);
		}
		this.objects = result;
		return this;
	}
}
