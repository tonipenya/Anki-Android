package com.ichi2.anki;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SearchProvider extends ContentProvider {
	public static final String AUTHORITY = "com.ichi2.anki.search";

	private static final String[] COLUMN_NAMES = new String[] { "_id",
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
	private static final int SEARCH_SUGGEST = 0;

	private static UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);

		String query = uri.getLastPathSegment();

		// The select will also match any html markup string
		// (e.g. in the capitals example the query 'sp' will
		// match all the cards containing '<span>').
		// TODO: Do a proper binding.
		// TODO: Add a method Card[] getCardsByQuestion(String) in Card class
		// and get the list with it instead of querying here.
		Cursor lcursor = AnkiDb.database.rawQuery("SELECT id, question, answer"
				+ " FROM cards WHERE question LIKE '%" + query + "%'", null);

		if (lcursor.moveToFirst()) {
			do {
				String question = lcursor.getString(1).replaceAll("\\<.*?\\>", "");
				
				// Small workaround to solve the html tag matching.
				if (!question.contains(query)) {
					continue;
				}
				
				Object[] rowObject = new Object[] { lcursor.getLong(0),
						question,
						lcursor.getString(2).replaceAll("\\<.*?\\>", ""),
						lcursor.getLong(0) };
				cursor.addRow(rowObject);
			} while (lcursor.moveToNext());
		}

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}