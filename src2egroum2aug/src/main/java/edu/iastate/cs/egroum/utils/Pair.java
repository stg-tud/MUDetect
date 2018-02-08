package edu.iastate.cs.egroum.utils;

/**
 * @author hoan
 *
 */
public class Pair {
	private Object obj1, obj2;
	private double weight = -1.0;
	
	public Pair(Object obj1, Object obj2)
	{
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	public Pair(Object obj1, Object obj2, double weight)
	{
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.weight = weight;
	}
	public double computeWeight(Pair other)
	{
		
		
		return this.weight;
	}
	public int compareTo(Pair other)
	{
		double otherWeight = other.getWeight();
		if(this.weight > otherWeight)
			return 1;
		else if(this.weight == otherWeight)
			return 0;
		else return -1;
		//return (int)(this.weight - other.getWeight());
	}
	public Object getObj1() {
		return obj1;
	}
	public void setObj1(Object obj1) {
		this.obj1 = obj1;
	}
	public Object getObj2() {
		return obj2;
	}
	public void setObj2(Object obj2) {
		this.obj2 = obj2;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	@Override
	public String toString()
	{
		return String.valueOf(this.weight);
	}
}

