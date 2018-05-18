package btcore.co.kr.h_fit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by leehaneul on 2018-02-06.
 */

public class DB_HeartRate extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final  String DBFILE_CONTAT = "heartRate.db";

    public DB_HeartRate(Context context) {
        super(context, DBFILE_CONTAT, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContactDBCHeart.SQL_CREATE_TBL) ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db) ;
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


}
