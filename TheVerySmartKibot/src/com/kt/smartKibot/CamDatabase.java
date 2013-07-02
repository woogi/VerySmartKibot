package com.kt.smartKibot;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class CamDatabase {
	private static final String TAG = "CameraTagDatabase";

	/* columns names */
	public static final String KEY_CLASS_ID = "class_id";
	public static final String KEY_TAG_NAME = "tag_name";
	public static final String KEY_COUNT_IN_CLASS = "count_in_class";
	public static final String KEY_DIRECTORY_PATH = "directory_path";
	public static final String KEY_TRAINING_IMAGE_PATH = "image_file_path";
	public static final String KEY_FRIEND_LEVEL = "friend_level";

	public static final String DATABASE_NAME = "camera_face_tag_database";
	private static final String DATABASE_TABLE_NAME = "tag_table";
	private static final int DATABASE_VERSION = 1;

	private final TagDatabaseOpenHelper mDatabaseOpenHelper;
	private static final HashMap<String, String> mColumnMap = buildColumnMap();

	public CamDatabase(Context context) {
		mDatabaseOpenHelper = new TagDatabaseOpenHelper(context);
	}

	/**
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(KEY_CLASS_ID, KEY_CLASS_ID);
		map.put(KEY_TAG_NAME, KEY_TAG_NAME);
		map.put(KEY_COUNT_IN_CLASS, KEY_COUNT_IN_CLASS);
		map.put(KEY_DIRECTORY_PATH, KEY_DIRECTORY_PATH);
		map.put(KEY_TRAINING_IMAGE_PATH, KEY_TRAINING_IMAGE_PATH);
		map.put(KEY_FRIEND_LEVEL, KEY_FRIEND_LEVEL);
		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		return map;
	}

	/**
	 * Returns a Cursor positioned at the word specified by rowId
	 * 
	 * @param rowId
	 *            id of word to retrieve
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor positioned to matching word, or null if not found.
	 */

	public Cursor getClassIDMatches(int classID) {
		Log.d(TAG, "-------------------getClassIDMatches-------------------------");
		String[] columns = new String[] { BaseColumns._ID, KEY_CLASS_ID, KEY_TAG_NAME, KEY_FRIEND_LEVEL, KEY_TRAINING_IMAGE_PATH };
		String selection = KEY_CLASS_ID + " LIKE ?";
		String[] selectionArgs = new String[] { Integer.toString(classID) + "%" };
		return query(selection, selectionArgs, columns, true, null);
	}

	public Cursor getTagMatches(String tagName) {
		Log.d(TAG, "-------------------getTagMatches-------------------------");
		String[] columns = new String[] { BaseColumns._ID, KEY_CLASS_ID, KEY_TAG_NAME, KEY_FRIEND_LEVEL, KEY_TRAINING_IMAGE_PATH };
		String selection = KEY_TAG_NAME + " LIKE ?";
		String[] selectionArgs = new String[] { tagName + "%" };
		return query(selection, selectionArgs, columns, true, null);
	}

	public Cursor getPathNClassID() {
		Log.d(TAG, "-------------------getPathNClassID-------------------------");
		String[] columns = new String[] { BaseColumns._ID, KEY_CLASS_ID, KEY_TRAINING_IMAGE_PATH };
		String selection = KEY_TRAINING_IMAGE_PATH + " LIKE ?";
		String[] selectionArgs = new String[] { "%" };
		return query(selection, selectionArgs, columns, true, KEY_CLASS_ID);
	}

	private Cursor query(String selection, String[] selectionArgs, String[] columns, boolean distinct, String groupBy) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DATABASE_TABLE_NAME);
		builder.setProjectionMap(mColumnMap);
		builder.setDistinct(distinct);
		SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
		Cursor cursor = builder.query(db, columns, selection, selectionArgs, groupBy, null, null);
		if (cursor == null) {
			Log.d(TAG, "-------------------cursor is null-------------------------");
			return null;
		} else if (!cursor.moveToFirst()) {
			Log.d(TAG, "-------------------cursor move first fail------cursor.getCount()=" + cursor.getCount());
			cursor.close();
			return null;
		}
		Log.d(TAG, "-------------------query success------cursor.getCount()=" + cursor.getCount());
		db.close();
		return cursor;
	}

	public void addItem(int classID, String tagName, int countInClass, String dirPath, String imageFilepath, int friendLevel) {
		long addedID;
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CLASS_ID, classID);
		initialValues.put(KEY_TAG_NAME, tagName);

		initialValues.put(KEY_COUNT_IN_CLASS, countInClass);
		initialValues.put(KEY_DIRECTORY_PATH, dirPath);
		initialValues.put(KEY_TRAINING_IMAGE_PATH, imageFilepath);

		initialValues.put(KEY_FRIEND_LEVEL, friendLevel);

		addedID = mDatabaseOpenHelper.getWritableDatabase().insert(DATABASE_TABLE_NAME, null, initialValues);
		Log.d(TAG, "addItem  addedID=" + addedID);

		mDatabaseOpenHelper.getWritableDatabase().close();
	}

	public void removeItem(String classID) {
		mDatabaseOpenHelper.getWritableDatabase().delete(DATABASE_TABLE_NAME, KEY_CLASS_ID + "=" + classID, null);
		Log.d(TAG, "removeItem  classID=" + classID);

		mDatabaseOpenHelper.getWritableDatabase().close();
	}

	/**
	 * This creates/opens the database.
	 */
	private static class TagDatabaseOpenHelper extends SQLiteOpenHelper {
		private SQLiteDatabase mDatabase;

		private static final String FTS_TABLE_CREATE = "create table " + DATABASE_TABLE_NAME + " (" + "_id integer primary key autoincrement, " + KEY_CLASS_ID + " int not null, " + KEY_TAG_NAME + " text not null," + KEY_COUNT_IN_CLASS + " int not null," + KEY_DIRECTORY_PATH + " text not null," + KEY_TRAINING_IMAGE_PATH + " text not null," + KEY_FRIEND_LEVEL + " int not null" + ");";

		TagDatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

			mDatabase = getWritableDatabase();
			mDatabase.close();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			Log.d(TAG, "TagDatabaseOpenHelper-------onCreate.......");
			mDatabase.execSQL(FTS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME);
			onCreate(db);
		}
	}

}
