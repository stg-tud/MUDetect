package mining;

public class Configuration {
    public int minPatternSize = 1, maxPatternSize = Integer.MAX_VALUE;
    public int minPatternSupport = 10, maxPatternSupport = 1000;

    /**
     * The miner should extend the pattern with an incoming data node even if the data node does not have a definition core action node 
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
    public boolean disallowRepeatedCalls = true;

    /**
     * Path to write mined patterns to. <code>null</code> to disable output.
     */
    public String outputPath = "output/patterns";
}
