/**
 * 
 */
package main;

import mining.Miner;

/**
 * @author hoan
 *
 */
public class Mine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Miner miner = new Miner("D:/systems/aspectj-1.5.3-src", "");
		//Miner miner = new Miner("input/Test2.java", "");
		miner.mine();
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000);
	}

}
