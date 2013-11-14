package com.fernando.agendachamada;
import android.app.ExpandableListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.CursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter;

public class ListaContatos extends ExpandableListActivity {

	 private static final String[] PROJECAO_CONTATOS = new String[] {
	        Contacts._ID,
	        Contacts.DISPLAY_NAME
	    };
	    private static final int GROUP_ID_COLUMN_INDEX = 0;

	    private static final String[] TELEFONE_PROJECAO = new String[] {
	            Phone._ID,
	            Phone.NUMBER
	    };

	    private static final int TOKEN_GROUP = 0;
	    private static final int TOKEN_CHILD = 1;

	    private static final class QueryHandler extends AsyncQueryHandler {
	        private CursorTreeAdapter mAdapter;

	        public QueryHandler(Context context, CursorTreeAdapter adapter) {
	            super(context.getContentResolver());
	            this.mAdapter = adapter;
	        }

	        @Override
	        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
	            switch (token) {
	            case TOKEN_GROUP:
	                mAdapter.setGroupCursor(cursor);
	                break;

	            case TOKEN_CHILD:
	                int groupPosition = (Integer) cookie;
	                mAdapter.setChildrenCursor(groupPosition, cursor);
	                break;
	            }
	        }
	    }

	    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

	        // ! construtor não pega um Cursor.
	        public MyExpandableListAdapter(Context context, int groupLayout,
	                int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
	                int[] childrenTo) {

	            super(context, null, groupLayout, groupFrom, groupTo, childLayout, childrenFrom,
	                    childrenTo);
	        }

	        @Override
	        protected Cursor getChildrenCursor(Cursor groupCursor) {
	            // Given the group, we return a cursor for all the children within that group 

	            // Return a cursor that points to this contact's phone numbers
	            Uri.Builder builder = Contacts.CONTENT_URI.buildUpon();
	            ContentUris.appendId(builder, groupCursor.getLong(GROUP_ID_COLUMN_INDEX));
	            builder.appendEncodedPath(Contacts.Data.CONTENT_DIRECTORY);
	            Uri phoneNumbersUri = builder.build();

	            mQueryHandler.startQuery(TOKEN_CHILD, groupCursor.getPosition(), phoneNumbersUri, 
	                    TELEFONE_PROJECAO, Phone.MIMETYPE + "=?", 
	                    new String[] { Phone.CONTENT_ITEM_TYPE }, null);

	            return null;
	        }
	    }

	    private QueryHandler mQueryHandler;
	    private CursorTreeAdapter mAdapter;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Set up our adapter
	        mAdapter = new MyExpandableListAdapter(
	                this,
	                android.R.layout.simple_expandable_list_item_1,
	                android.R.layout.simple_expandable_list_item_1,
	                new String[] { Contacts.DISPLAY_NAME }, // Name for group layouts
	                new int[] { android.R.id.text1 },
	                new String[] { Phone.NUMBER }, // Number for child layouts
	                new int[] { android.R.id.text1 });

	        setListAdapter(mAdapter);

	        mQueryHandler = new QueryHandler(this, mAdapter);

	        // Query for people
	        mQueryHandler.startQuery(TOKEN_GROUP, null, Contacts.CONTENT_URI, PROJECAO_CONTATOS, 
	                Contacts.HAS_PHONE_NUMBER + "=1", null, null);
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();

	        // Null out the group cursor. This will cause the group cursor and all of the child cursors
	        // to be closed.
	        mAdapter.changeCursor(null);
	        mAdapter = null;
	    }
	/*
    private int mGroupIdColumnIndex; 
    
    private String mPhoneNumberProjection[] = new String[] {
            People.Phones._ID, People.Phones.NUMBER
    };

    
    private ExpandableListAdapter mAdapter;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Query for people
        Cursor groupCursor = managedQuery(People.CONTENT_URI,
                new String[] {People._ID, People.NAME}, null, null, null);

        // Cache the ID column index
        mGroupIdColumnIndex = groupCursor.getColumnIndexOrThrow(People._ID);

        // Set up our adapter
        mAdapter = new MyExpandableListAdapter(groupCursor,
                this,
                android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_expandable_list_item_1,
                new String[] {People.NAME}, // Name for group layouts
                new int[] {android.R.id.text1},
                new String[] {People.NUMBER}, // Number for child layouts
                new int[] {android.R.id.text1});
        setListAdapter(mAdapter);
    }

    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

        public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout,
                int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
                int[] childrenTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom,
                    childrenTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // Given the group, we return a cursor for all the children within that group 

            // Return a cursor that points to this contact's phone numbers
            Uri.Builder builder = People.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, groupCursor.getLong(mGroupIdColumnIndex));
            builder.appendEncodedPath(People.Phones.CONTENT_DIRECTORY);
            Uri phoneNumbersUri = builder.build();

            // The returned Cursor MUST be managed by us, so we use Activity's helper
            // functionality to manage it for us.
            return managedQuery(phoneNumbersUri, mPhoneNumberProjection, null, null, null);
        }

    }
    */
}

