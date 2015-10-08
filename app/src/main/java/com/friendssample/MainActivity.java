package com.friendssample;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.friendssample.database.FriendsContentProvider;
import com.friendssample.database.FriendsTable;

import org.json.JSONArray;


public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private VkConnector mVkConnector;
    private ProgressDialog mProgressDialog;
    private SimpleCursorAdapter mCursorAdapter;
    private final static int FRIENDS_CURSOR_LOADER_ID = 1;
    private final static String SORTORDER_BUNDLEKEY = "sortBy";

    /**
     * RetainedFragment is used to store data across screen rotations.
     */
    private RetainedFragment mRetainedFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acquireRetainedFragment();
        mVkConnector = new VkConnector(this, getResources().getString(R.string.vk_app_id));
        createCursorAdapter();
        getLoaderManager().restartLoader(FRIENDS_CURSOR_LOADER_ID, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVkConnector.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVkConnector.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mVkConnector.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = mRetainedFragment.sortOrder;

        if (args != null) {
            sortOrder = args.getString(SORTORDER_BUNDLEKEY);
            mRetainedFragment.sortOrder = sortOrder;
        }

        return new CursorLoader(this, FriendsContentProvider.CONTENT_URI, /*projection*/ null, null,
                null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mCursorAdapter != null)
            mCursorAdapter.swapCursor(data);

        if (mCursorAdapter == null || mCursorAdapter.getCount() == 0)
            showSignInButton(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mCursorAdapter != null)
            mCursorAdapter.swapCursor(null);
    }

    private void acquireRetainedFragment() {
        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag("data");
        if (mRetainedFragment == null) {
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, "data").commit();
        }
    }

    public void onSignInClicked(View view) {
        mVkConnector.login(new VkConnector.Listener() {
            @Override
            public void onFriendsReceived(JSONArray friends) {
                putFriendsToDb(friends);
            }
        });
    }

    public void onSortClicked(View view) {

        Bundle sortByArgs = new Bundle();

        switch (view.getId()) {
            case R.id.sort_by_firstname :
                sortByArgs.putString(SORTORDER_BUNDLEKEY, FriendsTable.COLUMN_FIRSTNAME + " ASC");
                break;
            case R.id.sort_by_lastname:
                sortByArgs.putString(SORTORDER_BUNDLEKEY, FriendsTable.COLUMN_LASTNAME + " ASC");
                break;
            default :
                sortByArgs.putString(SORTORDER_BUNDLEKEY, FriendsTable.COLUMN_AGE + " DESC");
                break;
        }

        getLoaderManager().restartLoader(FRIENDS_CURSOR_LOADER_ID, sortByArgs, this);

    }

    private void createCursorAdapter() {
        String[] uiBindFrom = {FriendsTable.COLUMN_FIRSTNAME, FriendsTable.COLUMN_LASTNAME,
                FriendsTable.COLUMN_AGE, FriendsTable.COLUMN_IMAGE};
        int[] uiBindTo = {R.id.text1, R.id.text2, R.id.text3, R.id.photo};

        mCursorAdapter = new SimpleCursorAdapter(this, R.layout.listitem_friend, null,
                uiBindFrom, uiBindTo, 0);

        /**
         * ViewBinder to display photos from blob field FriendsTable.COLUMN_IMAGE
         */
        mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(FriendsTable.COLUMN_IMAGE)) {
                    byte[] rawImage = cursor.getBlob(columnIndex);
                    if (rawImage != null) {
                        Bitmap photo = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
                        if (view.getId() == R.id.photo) ((ImageView) view).setImageBitmap(photo);
                    }
                    return true;
                }
                return false;
            }
        });

        setListAdapter(mCursorAdapter);
    }

    public void onDbWriteCompleted() {
        if (mProgressDialog != null)
            mProgressDialog.hide();

        showSignInButton(false);
        getLoaderManager().restartLoader(FRIENDS_CURSOR_LOADER_ID, null, this);
    }

    private void showSignInButton(boolean show) {
        Button signInButton = (Button)findViewById(R.id.sign_in_button);
        View sortPanel = findViewById(R.id.sort_panel);

        if (show) {
            signInButton.setVisibility(View.VISIBLE);
            sortPanel.setVisibility(View.GONE);
        }
        else {
            signInButton.setVisibility(View.GONE);
            sortPanel.setVisibility(View.VISIBLE);
        }
    }

    private void putFriendsToDb(JSONArray friends) {
        createProgressDialog();
        mRetainedFragment.writeDbTask = new WriteDbTask(this);
        mRetainedFragment.writeDbTask.execute(friends);
    }

    public void onDbWriteProgress(int progress) {
        createProgressDialog();
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgress(progress);
    }

    private void createProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgress(0);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getResources().getString(R.string.write_db));
            mProgressDialog.setProgressNumberFormat(null);
            mProgressDialog.show();
        }
    }
}
