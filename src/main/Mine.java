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
		Miner miner = new Miner("D:/systems/aspectj-1.5.3-src");
		miner.mineJava();
	}

}
