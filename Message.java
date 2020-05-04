import java.io.Serializable;

/*
 * @Author: zhouzilong 
 * @Date: 2020-05-03 17:24:59 
 * @Last Modified by:   zhouzilong 
 * @Last Modified time: 2020-05-03 17:24:59 
 */

public class Message implements Serializable {

    /**
     * Message types and usage :
     * LIST : to list all the users in the room.
     * MESSAGE : normal message exchanged between server and client.
     * LOGOUT : to logout from the server.
     * SEARCH : to search the record for specific date.
     */
    private static final long serialVersionUID = 1L;
    
    static final int LIST = 0,  MESSAGE = 1,  LOGOUT = 2, SEARCH = 3;
    private int type;
    private String message;

    public Message(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }
    
}