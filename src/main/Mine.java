/**
 * 
 */
package main;

import java.util.ArrayList;

import egroum.EGroumBuilder;
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
		String path = "T:/repos/itext/5090/original-src", name = "itext";
//		String path = "input/Test2.java", name = "";
//		String path = "test/input/Test_mine.java", name = "";
		EGroumBuilder gb = new EGroumBuilder(null);
		Miner miner = new Miner(name);
		miner.mine(new ArrayList<>(gb.build(path)));
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000);
	}

}
