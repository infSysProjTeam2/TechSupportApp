package com.techsupportapp.utility;

public class DatabaseVariables {

    public static class Users {
        public static final String DATABASE_UNVERIFIED_USER_TABLE = "user_table/unverified_table";

        public static final String DATABASE_VERIFIED_SIMPLE_USER_TABLE = "user_table/verified_table/simple_user_table";
        public static final String DATABASE_VERIFIED_ADMIN_TABLE = "user_table/verified_table/admin_table";
        public static final String DATABASE_VERIFIED_WORKER_TABLE = "user_table/verified_table/worker_table";
        public static final String DATABASE_VERIFIED_CHIEF_TABLE = "user_table/verified_table/chief_table";
    }

    public static class Tickets {
        public static final String DATABASE_UNMARKED_TICKET_TABLE = "ticket_table/unmarked_table";
        public static final String DATABASE_MARKED_TICKET_TABLE = "ticket_table/marked_table";
        public static final String DATABASE_SOLVED_TICKET_TABLE = "ticket_table/solved_table";
    }

    public static class Indexes {
        public static final String DATABASE_TICKET_INDEX_COUNTER = "static_variables_table/ticket_index_counter";
        public static final String DATABASE_USER_INDEX_COUNTER = "static_variables_table/user_index_counter";
    }

}
