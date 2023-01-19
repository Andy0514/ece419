package shared.messages;
import shared.messages.KVMessage;
import java.io.InputStream;

public class KVMessageImpl implements KVMessage {

    private StatusType status;

    public KVMessageImpl(String key, String val) throws Exception {

    }

    public KVMessageImpl(InputStream socket_in) throws Exception {

    }

    public String getKey() {
        return "";
    }

    public String getValue() {
        return "";
    }

    public void setStatus(StatusType s) {
        status = s;
    }

    public StatusType getStatus() {
        return StatusType.GET_ERROR;
    }

    public byte[] getMsgBytes() {
        return null;
    }
}
