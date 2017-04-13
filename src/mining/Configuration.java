package mining;

public class Configuration {
    public int minPatternSize = 1, maxPatternSize = Integer.MAX_VALUE;
    public int minPatternSupport = 10, maxPatternSupport = 1000;

    /**
     * TODO what exactly is the impact of this flag?
     */
    public boolean extendSourceDataNodes = true;

    /**
     * Whether or not the miner should output log information to System.out.
     */
    public boolean disableSystemOut = false;
    
    /**
     * 
     */
    public boolean disAllowRepeatedCalls = true;

    /**
     * Path to write mined patterns to. <code>null</code> to disable output.
     */
    public String outputPath = "output/patterns";
}
