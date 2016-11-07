package com.techsupportapp.utility;

public class EventBusMessages {

    public class ListOfUsersMessage {
        public final String message;

        public ListOfUsersMessage(String message) {
            this.message = message;
        }
    }
}
