package de.tu_darmstadt.stg.mudetect.aug;

import de.tu_darmstadt.stg.mudetect.aug.builder.APIUsageExampleBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.*;
import org.junit.Test;


import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class SerializationTest {

     private static <T extends APIUsageGraph> byte[] serialize2ByteArray(T au){
         byte[] byteArray = {};

         try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream(bos)){
             oos.writeObject(au);
             oos.close();
             byteArray = bos.toByteArray();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return byteArray;
     }

    private static <T extends APIUsageGraph> T deserializeFromByteArrayToAU(byte[] byteArray){
        T obj = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
             ObjectInputStream ois = new ObjectInputStream(bis)){
            obj = (T) ois.readObject();
            ois.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static <T extends APIUsageGraph> void compareNodes(T graphWrote, T graphRead){
        /* So far a simple String matching test for nodes*/
        Set<String> readNodesAsString = graphRead.vertexSet().stream()
                .map(n -> n.toString())
                .collect(Collectors.toSet());

        Set<String> writeNodesAsString = graphWrote.vertexSet().stream()
                .map(n -> n.toString())
                .collect(Collectors.toSet());

        for(String strNode : writeNodesAsString){
            assertTrue(String.format("Expected that read nodes contains '%s'", strNode),
                    readNodesAsString.contains(strNode));
        }
        for(String strNode : readNodesAsString){
            assertTrue(String.format("Found unexpected node in read nodes '%s'", strNode),
                    writeNodesAsString.contains(strNode));
        }
    }

    private static <T extends APIUsageGraph> void compareEdges(T graphWrote, T graphRead){
        /* So far a simple String matching test for edges*/
        Set<String> readEdgesAsString = graphRead.edgeSet().stream()
                .map(e -> e.toString())
                .collect(Collectors.toSet());

        Set<String> writeEdgesAsString = graphWrote.edgeSet().stream()
                .map(e -> e.toString())
                .collect(Collectors.toSet());

        for(String strEdge : writeEdgesAsString) {
            assertTrue(String.format("Expected that read edges contains '%s'", strEdge),
                    readEdgesAsString.contains(strEdge));
        }
        for(String strEdge : readEdgesAsString) {
            assertTrue(String.format("Found unexpected edge in read edges '%s'", strEdge),
                    writeEdgesAsString.contains(strEdge));
        }
    }

    @Test
    public void testSerializeAndDeserializeAPIUsageExample(){
        String projectName = "dummyProject";
        String filePath = "a/b/c/d";
        String methodSignature = "dummyMethod";

        Location loc = new Location(projectName,filePath,methodSignature);

        APIUsageExampleBuilder builder = APIUsageExampleBuilder.buildAUG(loc)
                                        .withAssignment("A1",1)
                                        .withConstant("A2","int","VAL","1337")
                                        .withReceiverEdge("A2","A1")
                                        .withMethodCall("A3","T1","foo1",3)
                                        .withMethodCall("A4","T2","foo2",4)
                                        .withOrderEdge("A1","A3")
                                        .withOrderEdge("A3","A4");

        APIUsageExample aueWrote = builder.build();
        assertFalse(aueWrote==null);

        byte[] byteArray = serialize2ByteArray(aueWrote);

        assertTrue(byteArray.length>0);

        APIUsageExample aueRead = deserializeFromByteArrayToAU(byteArray);

        assertTrue(aueRead!=null);

        // check if Location could be deserialized

        String readProjectName = aueRead.getLocation().getProjectName();
        assertTrue(String.format("Expected Location project name was '%s' instead of '%s'", projectName, readProjectName),
                   readProjectName.equals(projectName));

        String readFilePath = aueRead.getLocation().getFilePath();
        assertTrue(String.format("Expected Location file path was '%s' instead of '%s'", filePath, readFilePath),
                   readFilePath.equals(filePath));

        String readMethodSignature = aueRead.getLocation().getMethodSignature();
        assertTrue(String.format("Expected Location project name was '%s' instead of '%s'", methodSignature, readMethodSignature),
                   readMethodSignature.equals("dummyMethod"));

        // check node sets
        compareEdges(aueWrote, aueRead);

        // check edge sets
        compareNodes(aueWrote, aueRead);
    }

    @Test
    public void testSerializeAndDeserializeAPIUsageGraph(){
        TestAUGBuilder builder = TestAUGBuilder.buildAUG()
                                               .withActionNodes("A", "B")
                                               .withDataNodes("D1", "D2","D3")
                                               .withEdge("A", PARAMETER, "B")
                                               .withEdge("A", ORDER, "B")
                                               .withEdge("D1",PARAMETER,"A")
                                               .withEdge("D2",PARAMETER,"A")
                                               .withEdge("D3",PARAMETER,"B");


        APIUsageGraph augWrote = builder.build();
        assertFalse(augWrote==null);

        byte[] byteArray = serialize2ByteArray(augWrote);
        assertTrue(byteArray.length>0);

        APIUsageExample augRead = deserializeFromByteArrayToAU(byteArray);

        // check node sets
        compareEdges(augWrote, augRead);

        // check edge sets
        compareNodes(augWrote, augRead);
    }
}
