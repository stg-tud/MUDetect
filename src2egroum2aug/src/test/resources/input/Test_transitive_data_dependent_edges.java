package input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

class Test_transitive_data_dependent_edges {

    byte[] pattern(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream dos = new ObjectOutputStream(baos);
        dos.writeObject(o);
        dos.close();
        return baos.toByteArray();
    }

}
