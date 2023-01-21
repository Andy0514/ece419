package shared.messages;
import shared.messages.KVMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

public class KVMessageImpl implements KVMessage, Serializable {

    private StatusType status;
    private String key;
    private String value;
    private byte[] msgBytes;
    private static final char LINE_FEED = 0x0A;
    private static final char RETURN = 0x0D;
    private static final int BUFFER_SIZE = 1024;
    private static final int DROP_SIZE = 1024 * BUFFER_SIZE;

    public KVMessageImpl(String key, String val, StatusType status) throws Exception {
        if(key.length() > 20){ // max key length = 20 Bytes, each char = 1 bytes
            throw new Exception("Exceed max key length");
        }
        if(val.length() > 120000) { // max val length = 120k Bytes
            throw new Exception("Exceed max value length");
        }
        this.key = key;
        this.value = val;
        this.status = status;
        this.msgBytes = toByteArray(key, val, status);

    }

    public KVMessageImpl(InputStream socket_in) throws IOException {

        int index = 0;
        byte[] tmp = null;
        byte[] bufferBytes = new byte[BUFFER_SIZE];
        msgBytes = new byte[4];
        /* read first char from stream */
        byte read = (byte) socket_in.read();
        // read the key length (1 byte)
        int keyLength = read;
        msgBytes[0] = read;
        // read the value length (3 bytes)
        read = (byte) socket_in.read();
        int valLength = read << 16;
        msgBytes[1] = read;
        read = (byte) socket_in.read();
        valLength += read << 8;
        msgBytes[2] = read;
        read = (byte) socket_in.read();
        valLength += read;
        msgBytes[3] = read;

        /* ===========read key from stream ===================*/
        read = (byte) socket_in.read();
        int readCount = 0;
        while(readCount < keyLength) {/* read the key until carriage return */
            /* only read valid characters, i.e. letters and numbers */
            if((read > 31 && read < 127)) { /* key stream always smaller than buffer*/
                bufferBytes[index] = read;
                index++;
            }
            /* read next char from stream */
            read = (byte) socket_in.read();
            readCount++;
        }
        if(read != 13){ /* carriage return */
            throw new RuntimeException("Key Reading Error");
        }
        byte[] keyBytes = new byte[index];
        System.arraycopy(bufferBytes, 0, keyBytes, 0, index);
        key = new String(keyBytes);
        tmp = new byte[keyBytes.length + msgBytes.length];
        System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
        System.arraycopy(keyBytes, 0, tmp, msgBytes.length, keyBytes.length);


        /* =================read value from stream=============== */
        read = (byte) socket_in.read();
        boolean reading = true;
        readCount = 0;
        byte[] valBytes = null;
        while(readCount < valLength && reading) {/* read the key until carriage return */
            /* if buffer filled, copy to msg array */
            if(index == BUFFER_SIZE) {
                if(valBytes == null){
                    tmp = new byte[BUFFER_SIZE];
                    System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
                } else {
                    tmp = new byte[valBytes.length + BUFFER_SIZE];
                    System.arraycopy(valBytes, 0, tmp, 0, valBytes.length);
                    System.arraycopy(bufferBytes, 0, tmp, valBytes.length,
                            BUFFER_SIZE);
                }

                valBytes = tmp;
                bufferBytes = new byte[BUFFER_SIZE];
                index = 0;
            }

            /* only read valid characters, i.e. letters and numbers */
            if((read > 31 && read < 127)) {
                bufferBytes[index] = read;
                index++;
            }

            /* stop reading if DROP_SIZE is reached */
            if(valBytes != null && valBytes.length + index >= DROP_SIZE) {
                reading = false;
            }

            /* read next char from stream */
            read = (byte) socket_in.read();
            readCount++;
        }
        if(reading){
            if(read != 13){ /* carriage return */
                throw new RuntimeException("Val Reading Error");
            }
        }
        if(valBytes == null){
            tmp = new byte[index];
            System.arraycopy(bufferBytes, 0, tmp, 0, index);
        } else {
            tmp = new byte[valBytes.length + index];
            System.arraycopy(valBytes, 0, tmp, 0, valBytes.length);
            System.arraycopy(bufferBytes, 0, tmp, valBytes.length, index);
        }

        value = new String(tmp);

        tmp = new byte[valBytes.length + msgBytes.length];
        System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
        System.arraycopy(valBytes, 0, tmp, msgBytes.length, valBytes.length);

        /* ===============read status ==============*/
        read = (byte) socket_in.read();
        if((byte) socket_in.read() != 13){
            throw new RuntimeException("Status Reading Error");
        }
        status = StatusType.fromInteger(read);

        /* get final msgBytes */
        tmp = Arrays.copyOf(msgBytes, msgBytes.length + 1);
        tmp[tmp.length - 1] = read;
        msgBytes = tmp;

    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public StatusType getStatus() {
        return this.status;
    }

    public byte[] getMsgBytes() {

        return this.msgBytes;
    }

    public void setStatus(StatusType s) {
        this.status = s;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setValue(String value) {
        this.value = value;
    }
    private byte[] addCtrChars(byte[] bytes) {
        byte[] ctrBytes = new byte[]{LINE_FEED, RETURN};
        byte[] tmp = new byte[bytes.length + ctrBytes.length];

        System.arraycopy(bytes, 0, tmp, 0, bytes.length);
        System.arraycopy(ctrBytes, 0, tmp, bytes.length, ctrBytes.length);

        return tmp;
    }

    /*
    * msgByteArray:
    * keyLength + valueLength + keyBytes + ctrBytes + valueBytes + ctrBytes + statusBytes + ctrBytes
    */
    private byte[] toByteArray(String k, String v, StatusType s){
        byte[] keyLength = new byte[]{(byte)(k.length() & 0xFF)}; // k.length <= 20, 1 byte
        byte[] valueLength = new byte[]{(byte) ((v.length() & 0xFF0000) >> 16), (byte) ((v.length() & 0xFF00) >> 8), (byte)(v.length() & 0x00FF)}; // v.length <= 120000, 3 Bytes
        byte[] keyBytes = k.getBytes();
        byte[] valueBytes = v.getBytes();
        byte[] statusBytes = new byte[]{(byte)(StatusType.toInt(s) & 0xFF)}; // statusBytes 0-8, 1 Byte
//        byte[] ctrBytes = new byte[]{LINE_FEED, RETURN};
        byte[] ctrBytes = new byte[]{RETURN};
//        byte[] tmp = new byte[keyLength.length + valueLength.length + keyBytes.length + valueBytes.length + statusBytes.length + 5 * ctrBytes.length ];
        byte[] tmp = new byte[keyLength.length + valueLength.length + keyBytes.length + valueBytes.length + statusBytes.length + 3 * ctrBytes.length ];

        System.arraycopy(keyLength, 0, tmp, 0, keyLength.length);
        System.arraycopy(valueLength, 0, tmp, keyLength.length, valueLength.length);
        System.arraycopy(keyBytes, 0, tmp, keyLength.length + valueLength.length, keyBytes.length);
        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + valueLength.length + keyBytes.length, ctrBytes.length);
        System.arraycopy(valueBytes, 0, tmp, keyLength.length + valueLength.length + keyBytes.length  + ctrBytes.length, valueBytes.length);
        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + valueLength.length + keyBytes.length + valueBytes.length + ctrBytes.length, ctrBytes.length);
        System.arraycopy(statusBytes, 0, tmp, keyLength.length + valueLength.length + keyBytes.length + valueBytes.length + 2 * ctrBytes.length, statusBytes.length);
        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + valueLength.length + keyBytes.length + valueBytes.length + statusBytes.length + 2 * ctrBytes.length, ctrBytes.length);

//        System.arraycopy(keyLength, 0, tmp, 0, keyLength.length);
//        System.arraycopy(ctrBytes, 0, tmp, keyLength.length, ctrBytes.length);
//        System.arraycopy(valueLength, 0, tmp, keyLength.length + ctrBytes.length, valueLength.length);
//        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + ctrBytes.length + valueLength.length, ctrBytes.length);
//        System.arraycopy(keyBytes, 0, tmp, keyLength.length + 2 * ctrBytes.length + valueLength.length, keyBytes.length);
//        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + 2 * ctrBytes.length + valueLength.length + keyBytes.length, ctrBytes.length);
//        System.arraycopy(valueBytes, 0, tmp, keyLength.length + 3 * ctrBytes.length + valueLength.length + keyBytes.length, valueBytes.length);
//        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + 3 * ctrBytes.length + valueLength.length + keyBytes.length + valueBytes.length, ctrBytes.length);
//        System.arraycopy(statusBytes, 0, tmp, keyLength.length + 4 * ctrBytes.length + valueLength.length + keyBytes.length + valueBytes.length, statusBytes.length);
//        System.arraycopy(ctrBytes, 0, tmp, keyLength.length + 4 * ctrBytes.length + valueLength.length + keyBytes.length + valueBytes.length + statusBytes.length, ctrBytes.length);

        return tmp;
    }

}
