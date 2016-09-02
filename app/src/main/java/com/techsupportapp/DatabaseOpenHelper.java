package com.techsupportapp;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper implements BaseColumns{

    //region Fields

    private static final String PERSONALITY_TABLE = "personality_table";

    private static final String LOGIN_COLUMN = "login";
    private static final String PASSWORD_COLUMN = "password";

    private static final String USER_TABLE = "user_table";

    private static final String SURNAME_COLUMN = "surname";
    private static final String NAME_COLUMN = "name";
    private static final String IS_ADMIN_COLUMN = "is_admin";
    private static final String COMPANY_ID_COLUMN = "company_id";

    //endregion

    //region Scripts

    private static final String DATABASE_CREATE_USER_TABLE = "create table " + USER_TABLE + " ("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + SURNAME_COLUMN + " text not null, "
            + NAME_COLUMN + " text not null, "
            + IS_ADMIN_COLUMN + " text not null, "
            + COMPANY_ID_COLUMN + " integer not null);";

    //endregion

    public DatabaseOpenHelper(Context context, String name, int version){
        super(context, name, null, version);
    }

    public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_USER_TABLE); //TODO исправить после организации скачивания базы данных с сервера
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        db.execSQL("DROP TABLE IF IT EXISTS " + USER_TABLE);

        onCreate(db);
    }

}
