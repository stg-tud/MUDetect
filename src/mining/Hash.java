package mining;

import java.util.HashMap;
import java.util.Random;

import exas.ExasFeature;

/**
 * @author Nguyen Thanh Tung
 *
 */
public class Hash {
	//public static final int prime = 1048573; //20 bit
	public static final int prime = 16777213; //24 bit
	public static final int max_bits = 100;
	public static final int max_uHash = 20;
	public static final int max_gHash = 512;
	public static final int max_vSize = 2500;
	public static Random randGaussian = new Random();
	//public static MRG32k3a randManhattan = new MRG32k3a();
	public static int[] ran;
	public static int vectorSize, uHashSize, gHashSize = max_gHash; // 9 bit	
	public static double wSize;
	public static double b[][];
	public static double[][][] aGaussian;
	/**
	 * This method will be used to initialize the hashing parameters
	 * @param L <= 512
	 * @param k <= 20
	 * @param w
	 */
	public static void init(int gHash, int uHash, double windowSize, int vSize)
	{
		gHashSize = gHash;
		uHashSize = uHash;
		wSize = windowSize;
		vectorSize = vSize;
		randGaussian = new Random(prime);
		ran = new int[uHash];
		
		for (int i = 0; i < ran.length; i++)	
			ran[i] = randomInt(0, prime);
		
		b = new double[gHash][uHash];
		aGaussian = new double[gHash][uHash][vSize];
		
		for(int i = 0; i < gHash; i++)
		{
			for(int j = 0; j < uHash; j++) {
				b[i][j] = randomDouble(0, windowSize);
				for (int l = 0; l < vSize; l++)
					aGaussian[i][j][l] = randGaussian.nextGaussian();
			}
		}
	}
	/*public static void init(int L, int k, int vSize)
	{
		gHashSize = L;
		uHashSize = k;
		vectorSize = vSize;
		
		//ran = new int[uHashSize];
		ran = new int[max_uHash];
		for (int i = 0; i < ran.length; i++)	
			ran[i] = (int) randomInt(0, prime);
		
		
		//b = new double[gHashSize][uHashSize];
		b = new double[max_gHash][max_uHash];
		//aGaussian = new double[gHashSize][uHashSize][vectorSize];
		aGaussian = new double[max_gHash][max_uHash][max_vSize];
		System.out.println(Hash.aGaussian[0][0].length);
		
		for(int i = 0; i < max_gHash; i++)
		{
			for(int j = 0; j < max_uHash; j++) {
				b[i][j] = Hash.randomDouble(0, wSize);
				for (int l = 0; l < max_vSize; l++)
					aGaussian[i][j][l] = randGaussian.nextGaussian();
			}
		}
	}*/
	public static void reset(int L, int k, int vSize)
	{
		if(vectorSize < vSize) {
			int oldVectorSize = vectorSize;
			vectorSize = vSize;
			System.out.println("Vector size changed!");
			double[][][] a1 = aGaussian.clone();
			//aGaussian = new double[gHashSize][uHashSize][vectorSize];
			aGaussian = new double[max_gHash][max_uHash][vectorSize];
			
			for(int i = 0; i < max_gHash; i++) {
				for(int j = 0; j < max_uHash; j++) {
					for (int l = 0; l < oldVectorSize; l++)
						aGaussian[i][j][l] = a1[i][j][l];
					for (int l = oldVectorSize; l < vectorSize; l++)
						aGaussian[i][j][l] = randGaussian.nextGaussian();
				}
			}
		}
	}
	public static double getWSize() {
		return wSize;
	}
	public static void setWSize(double size) {
		wSize = size;
	}
	public int[] hashEuclidean(Fragment frag)
	{
		int result[] = new int[gHashSize]; 
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (int j = 0; j < Hash.uHashSize; j++)
				result[i] += Hash.ran[j] * hash(frag.getVector(), i, j);
			result[i] %= Hash.prime;
			if (result[i] < 0) result[i] += Hash.prime;
			result[i] += i << 24;
		}
		return result;
	}
	public int[] hashManhattan(Fragment frag)
	{
		if (frag.getVector() == null)
			System.out.println("ERROR");
		int result[] = new int[gHashSize]; 
		//int tmpVector[] = new int[max_bits];
		HashMap<ExasFeature, Integer> tmpVector = new HashMap<ExasFeature, Integer>(frag.getVector());

		/*for(int i = 0; i < tmpVector.length; i++)
		{
			tmpVector[i] = (dotProduct(aTemp[i], frag.getVector()) <= 0 ? 0 : 1);
		}*/
		for(ExasFeature key : tmpVector.keySet())
			tmpVector.put(key, 1);
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (int j = 0; j < Hash.uHashSize; j++)
				//result[i] += Hash.ran[j] * hashManhattan(tmpVector, i, j);
				result[i] += Hash.ran[j] * hash(tmpVector, i, j);
			result[i] %= Hash.prime;
			if (result[i] < 0) result[i] += Hash.prime;
			result[i] += i << 24;
		}
		return result;
	}
	public String toString() {
		return "Hash scheme for " + gHashSize + " hash functions ";
	}
	public static double randomDouble(double minValue, double maxValue) 
	{
		Random rand = new Random();
		return rand.nextDouble() * (maxValue - minValue) + minValue;
	}	
	public static int randomInt(int minValue, int maxValue)
	{
		Random rand = new Random();
		return (int) Math.round(rand.nextDouble() * (maxValue - minValue) + minValue);
	}
	public double dotProduct(double[] a, short[] v) 
	{
		double result = 0;
		for (int i = 0; i < Hash.vectorSize; i++)
			result += a[i] * v[i];
		return result;
	}
	public double dotProduct(double[] a, float[] v) 
	{
		double result = 0;
		for (int i = 0; i < Hash.vectorSize; i++)
			result += a[i] * v[i];
		return result;
	}
	public double dotProduct(double[] a, double[] v)
	{
		double result = 0;
		for (int i = 0; i < Hash.vectorSize; i++)
			result += a[i] * v[i];
		return result;
	}
	public double dotProduct(double[] a, HashMap<ExasFeature, Integer> v)
	{
		double result = 0;
		for(ExasFeature key : v.keySet())
			result += a[key.getId()] * v.get(key);
		return result;
	}
	public int hash(short[] p, int hid, int lineID) 
	{
		double dot = dotProduct(aGaussian[hid][lineID], p) + b[hid][lineID];
		return (int) Math.round(dot/wSize);
	}
	public int hash(float[] p, int hid, int lineID) 
	{
		double dot = dotProduct(aGaussian[hid][lineID], p) + b[hid][lineID];
		return (int) Math.round(dot/Hash.wSize);
	}
	public int hash(double[] p, int hid, int lineID)
	{
		double dot = dotProduct(aGaussian[hid][lineID], p) + b[hid][lineID];
		return (int) Math.round(dot/Hash.wSize);
	}
	public int hash(HashMap<ExasFeature, Integer> p, int hid, int lineID)
	{
		double dot = dotProduct(aGaussian[hid][lineID], p) + b[hid][lineID];
		return (int) Math.round(dot/Hash.wSize);
	}
	public static double nextRandomVariable(double m, double s) {
        double v;
        double u1;

        do {
            u1 = randomDouble(0.0, 1.0);
            double u2 = randomDouble(0.0, 1.0);
            v = 2.0 * u2 - 1.0;
        } while (u1 * u1 + v * v >= 1);

        return (v / u1) * s + m;
    }
}
