package server.utils;

import java.io.*;

/**
 * Handles message serialization and deserialization.
 * Single Responsibility: Converts objects to/from byte arrays for network transmission.
 */
public class MessageSerializer {
    
    /**
     * Serialize object to byte array
     */
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Deserialize byte array to object
     */
    public static Object deserialize(Object data) throws IOException, ClassNotFoundException {
        if (data instanceof byte[]) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                
                return ois.readObject();
            }
        }
        return data;
    }
    
    /**
     * Check if object needs deserialization
     */
    public static boolean needsDeserialization(Object obj) {
        return obj instanceof byte[];
    }
}