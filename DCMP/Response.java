package DCMP;

import java.io.Serializable;

public class Response implements Serializable {
    public boolean ack = true;
    public Response(boolean ack) {
        this.ack = ack;
    }
}
