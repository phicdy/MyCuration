package com.example.rssfilterreader;
  
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
  
public class DatabaseHelper extends SQLiteOpenHelper{
  
    public DatabaseHelper(Context context) {
        //String extraStoragePath = Environment.getExternalStorageDirectory()+"/rss_manage";
        super(context, "rss_manage", null, 3);
    }
  
    //onCreate() is called when database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFeedsTableSQL = 
                "create table feeds(_id integer primary key autoincrement,"+
                "title text,"+
                "url text,"+
                "format text)";
        String createArticlesTableSQL =
                "create table articles(_id integer primary key autoincrement,"+
                "title text,"+
                "url text,"+
                "status text default "+ Article.UNREAD+","+
                "point text,"+
                "date text,"+
                "feedId integer,"+
                "foreign key(feedId) references feeds(_id))";
        String createFiltersTableSQL =
                "create table filters(_id integer primary key autoincrement,"+
                "feedId integer,"+
                "keyword text,"+
                "url text," +
                "title text,"+
                "foreign key(feedId) references feeds(_id))";
        String createPrioritiesTableSQL =
                "create table priorities(_id integer primary key autoincrement,"+
                "priorFeedId integer,"+
                "posteriorFeedId integer,"+
                "foreign key(priorFeedId) references feeds(_id),"+
                "foreign key(posteriorFeedId) references feeds(_id))";
                  
        db.execSQL(createFeedsTableSQL);
        db.execSQL(createArticlesTableSQL);
        db.execSQL(createFiltersTableSQL);
        db.execSQL(createPrioritiesTableSQL);
    }
      
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
  
    //onUpgrade() is called when database version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // TODO Auto-generated method stub
//        if(oldVersion < newVersion) {
//        String sql = "alter table filters add title text";
//        db.execSQL(sql);
//        }
    }
  
}
