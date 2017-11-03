package io.skytreasure.kotlingroupchat.chat.ui.widget;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import io.skytreasure.kotlingroupchat.chat.model.MessageModel;

/**
 * Created by natuan on 17/01/07.
 * Modified by akash on 02/11/17
 */

public class InfiniteFirebaseArray implements ChildEventListener, ValueEventListener {

    public static final int ADDED = 0;
    public static final int CHANGED = 1;
    public static final int REMOVED = 2;
    public static final int NOTIFY_ALL = 3;

    List<DataSnapshot> tempList = new ArrayList<>();


    @IntDef({ADDED, CHANGED, REMOVED, NOTIFY_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    public interface OnChangedListener {

        void onChanged(@EventType int type, int index, int oldIndex);

        void onCancelled(DatabaseError databaseError);
    }

    private Query mQuery;
    public List<DataSnapshot> mSnapshots = new ArrayList<>();
    private int mIndex = -1;
    private String mNextChildKey = null;
    private String mEndKey;
    private static String startAt;
    private int mNumberPerPage;
    private int mCount;
    private boolean isDuplicateKey;
    private OnChangedListener mListener;

    public InfiniteFirebaseArray(Query ref, int numberPerPage, String startat) {
        mNumberPerPage = numberPerPage;
        startAt = startat;
        initQuery(ref);
    }

    private void initQuery(Query ref) {
        Logger.d("LastChildKey: " + mNextChildKey);
        Logger.d("NumberPerPage: " + mNumberPerPage);
        mQuery = ref.orderByChild("timestamp").startAt(startAt).limitToLast(mNumberPerPage);
        mQuery.addChildEventListener(this);
        mCount = 0;
        tempList.clear();
    }

    private void initNextQuery(Query ref) {
        Logger.d("LastChildKey: " + mNextChildKey);
        Logger.d("NumberPerPage: " + mNumberPerPage);
        ref.orderByChild("timestamp").limitToLast(5)
                .startAt(startAt).endAt(mEndKey).addListenerForSingleValueEvent(this);
        mCount = 0;
        tempList.clear();
    }

    public void cleanup() {
        Logger.enter();
        //mQuery.removeEventListener(this);
        Logger.exit();
    }

    public void more(Query ref) {
        if (/*isHasMore()*/true) {
            initNextQuery(ref);
        }
    }

    public int getCount() {
        return mSnapshots.size();
    }

    public DataSnapshot getItem(int index) {
        return mSnapshots.get(index);
    }


    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
        if (snapshot == null) {
            return;
        }
        mCount++;
        if (mCount == 1) {
            mEndKey = snapshot.getValue(MessageModel.class).getTimestamp();
        }
        mNextChildKey = snapshot.getKey();
        /*if (mCount > mNumberPerPage) {
            return;
        }*/
        if (checkDuplicateKey(mNextChildKey)) {
            isDuplicateKey = true;
            return;
        }
      /*  tempList.clear();
        tempList.add(snapshot);
        //if (mCount == mNumberPerPage) {
        mSnapshots.addAll(0, tempList);
        mListener.onChanged(ADDED, 0, -1);*/
        //}
        //mSnapshots.addAll(0, tempList);
        tempList.clear();
        tempList.add(snapshot);
        int size = mSnapshots.size();
        mSnapshots.addAll(size, tempList);
        notifyChangedListeners(ADDED, mSnapshots.size());
        //notifyChangedListeners(ADDED, 0);

        Logger.d(mIndex + " : " + mNextChildKey);
    }

    private boolean checkDuplicateKey(String nextChildKey) {
        if (mSnapshots.size() > 0) {
            DataSnapshot previousSnapshot = mSnapshots.get(0);
            String previousChildkey = previousSnapshot == null ? "" : previousSnapshot.getKey();
            return (!TextUtils.isEmpty(previousChildkey) && previousChildkey.equals(nextChildKey));
        }
        return false;
    }

    public boolean isHasMore() {
        boolean isHasMore = true;
        if (mCount < mNumberPerPage || isDuplicateKey) {
            isHasMore = false;
        }
        Logger.d("isHasMore: " + isHasMore);
        return isHasMore;
    }

    public void setOnChangedListener(OnChangedListener listener) {
        mListener = listener;
    }

    private void notifyChangedListeners(@EventType int type, int index) {
        notifyChangedListeners(type, index, -1);
    }

    protected void notifyChangedListeners(@EventType int type, int index, int oldIndex) {
        if (mListener != null) {
            mListener.onChanged(type, index, oldIndex);
        }
    }

    protected void notifyCancelledListeners(DatabaseError databaseError) {
        if (mListener != null) {
            mListener.onCancelled(databaseError);
        }
    }

    private int getIndexForKey(String key) {
        int index = 0;
        for (DataSnapshot snapshot : mSnapshots) {
            if (snapshot.getKey().equalsIgnoreCase(key)) {
                return index;
            } else {
                index++;
            }
        }
        return -1;
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String s) {
        int index = getIndexForKey(snapshot.getKey());
        if (index != -1) {
            mSnapshots.set(index, snapshot);
            notifyChangedListeners(CHANGED, index);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        int index = getIndexForKey(snapshot.getKey());
        if (index != -1) {
            mSnapshots.remove(index);
            notifyChangedListeners(REMOVED, index);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            mCount = 0;

            for (DataSnapshot currentSnap : dataSnapshot.getChildren()) {
                //MessageModel tempModel=dataSnapshot.getChildren().iterator().next().getValue(MessageModel.class);
                if (mCount == 0) {
                    mEndKey = currentSnap.getValue(MessageModel.class).getTimestamp();
                }
                mNextChildKey = currentSnap.getKey();
                if (!checkDuplicateKey(mNextChildKey)) {
                    tempList.add(currentSnap);
                    mIndex++;
                }
                mCount++;
            }
            mSnapshots.addAll(0, tempList);
            mListener.onChanged(NOTIFY_ALL, 0, -1);

        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        notifyCancelledListeners(databaseError);
    }
}
