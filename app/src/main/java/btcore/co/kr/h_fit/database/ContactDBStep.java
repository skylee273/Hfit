package btcore.co.kr.h_fit.database;

/**
 * Created by leehaneul on 2018-02-07.
 */

public class ContactDBStep {
    private ContactDBStep(){};

    public static final String TBL_CONTACT = "STEP";
    public static final String COL_DATE = "STEPDATE" ;
    public static final String COL_WEEK = "WEEK" ;
    public static final String COL_HOUR = "HOUR" ;
    public static final String COL_STEP = "STEPDATA" ;


    public static final String SQL_CREATE_TBL = "CREATE TABLE IF NOT EXISTS " + TBL_CONTACT + " " +
            "(" +
            COL_DATE + " TEXT" + ", " +
            COL_WEEK + " INTEGER NOT NULL" + ", " +
            COL_HOUR + " INTEGER NOT NULL" + ", " +
            COL_STEP + " INTEGER NOT NULL" +
            ")";

    public static final String SQL_DROP_TBL = "DROP TABLE IF EXISTS " + TBL_CONTACT;

    public static final String SQL_SELECT = "SELECT * FROM " + TBL_CONTACT + " ORDER BY " + COL_HOUR + " ASC";

    public static final String SQL_SELECT_ASC_WEEK = "SELECT * FROM " + TBL_CONTACT + " ORDER BY " + COL_WEEK + " ASC";

    public static final String SQL_SELECT_ASC_DATE = "SELECT * FROM " + TBL_CONTACT + " ORDER BY " + COL_DATE + " ASC";

    public static final String SQL_SELECT_TODAY = "SELECT * FROM " + TBL_CONTACT + " WHERE " + COL_DATE + " = " ;


    public static final String SQL_INSERT = "INSERT OR REPLACE INTO " + TBL_CONTACT + " " +
            "(" + COL_DATE + ", " + COL_WEEK + ", " + COL_HOUR + ", " + COL_STEP + ") VALUES " ;


    public static final String SQL_DELETE = "DELETE FROM " + TBL_CONTACT;



}
