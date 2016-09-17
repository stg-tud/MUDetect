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
		//Miner miner = new Miner("T:/repos/itext/5090/original-src", "itext");
		//Miner miner = new Miner("input/Test2.java", "");
		Miner miner = new Miner("test/input/Test_mine.java", "");
		miner.mine();
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000);
	}

}
