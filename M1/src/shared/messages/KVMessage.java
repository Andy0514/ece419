package shared.messages;

public interface KVMessage {
	
	public enum StatusType {
		GET, 			/* Get - request */
		GET_ERROR, 		/* requested tuple (i.e. value) not found */
		GET_SUCCESS, 	/* requested tuple (i.e. value) found */
		PUT, 			/* Put - request */
		PUT_SUCCESS, 	/* Put - request successful, tuple inserted */
		PUT_UPDATE, 	/* Put - request successful, i.e. value updated */
		PUT_ERROR, 		/* Put - request not successful */
		DELETE_SUCCESS, /* Delete - request successful */
		DELETE_ERROR; 	/* Delete - request successful */

		public static StatusType fromInteger(int x) {
			switch(x) {
				case 0:
					return GET;
				case 1:
					return GET_ERROR;
				case 2:
					return GET_SUCCESS;
				case 3:
					return PUT;
				case 4:
					return PUT_SUCCESS;
				case 5:
					return PUT_UPDATE;
				case 6:
					return PUT_ERROR;
				case 7:
					return DELETE_SUCCESS;
				case 8:
					return DELETE_ERROR;
			}
			return null;
		}

		public static int toInt(StatusType x) {
			switch(x) {
				case GET:
					return 0;
				case GET_ERROR:
					return 1;
				case GET_SUCCESS:
					return 2;
				case PUT:
					return 3;
				case PUT_SUCCESS:
					return 4;
				case PUT_UPDATE:
					return 5;
				case PUT_ERROR:
					return 6;
				case DELETE_SUCCESS:
					return 7;
				case DELETE_ERROR:
					return 8;
			}
			return -1;
		}
	}

	/**
	 * @return the key that is associated with this message, 
	 * 		null if not key is associated.
	 */
	public String getKey();
	
	/**
	 * @return the value that is associated with this message, 
	 * 		null if not value is associated.
	 */
	public String getValue();
	
	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus();
	
}


