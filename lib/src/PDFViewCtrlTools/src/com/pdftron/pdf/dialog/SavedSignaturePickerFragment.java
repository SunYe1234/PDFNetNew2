package com.pdftron.pdf.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.pdftron.pdf.adapter.SavedSignatureAdapter;
import com.pdftron.pdf.interfaces.OnSavedSignatureListener;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.CustomAsyncTask;
import com.pdftron.pdf.utils.StampManager;
import com.pdftron.pdf.utils.ToolbarActionMode;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ItemClickHelper;
import com.pdftron.pdf.widget.recyclerview.ItemSelectionHelper;
import com.pdftron.pdf.widget.recyclerview.SimpleRecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SavedSignaturePickerFragment extends Fragment {

    private OnSavedSignatureListener mOnSavedSignatureListener;

    private SavedSignatureAdapter mSavedSignatureAdapter;
    private ItemSelectionHelper mItemSelectionHelper;
    private ToolbarActionMode mActionMode;
    private Toolbar mToolbar;
    private Toolbar mCabToolbar;

    private AttachSignatureTask mAttachSignatureTask;

    public static SavedSignaturePickerFragment newInstance() {
        return new SavedSignaturePickerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_rubber_stamp_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.add_custom_stamp_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSavedSignatureListener != null) {
                    mOnSavedSignatureListener.onCreateSignatureClicked();
                }
            }
        });

        SimpleRecyclerView recyclerView = view.findViewById(R.id.stamp_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        ItemClickHelper itemClickHelper = new ItemClickHelper();
        itemClickHelper.attachToRecyclerView(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                if (mActionMode == null) {
                    File file = mSavedSignatureAdapter.getFileItem(position);
                    if (mOnSavedSignatureListener != null && file != null) {
                        mOnSavedSignatureListener.onSignatureSelected(file.getAbsolutePath());
                    }
                } else {
                    mItemSelectionHelper.setItemChecked(position, !mItemSelectionHelper.isItemChecked(position));
                    mActionMode.invalidate();
                }
            }
        });

        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView recyclerView, View view, int position, long id) {
                if (mActionMode == null) {
                    mItemSelectionHelper.setItemChecked(position, true);
                    mActionMode = new ToolbarActionMode(view.getContext(), mCabToolbar);
                    mActionMode.startActionMode(mActionModeCallback);
                    return true;
                }
                return false;
            }
        });

        mItemSelectionHelper = new ItemSelectionHelper();
        mItemSelectionHelper.attachToRecyclerView(recyclerView);
        mItemSelectionHelper.setChoiceMode(ItemSelectionHelper.CHOICE_MODE_MULTIPLE);

        mSavedSignatureAdapter = new SavedSignatureAdapter(view.getContext(), mItemSelectionHelper);
        mSavedSignatureAdapter.registerAdapterDataObserver(mItemSelectionHelper.getDataObserver());
        recyclerView.setAdapter(mSavedSignatureAdapter);

        TextView emptyTextView = view.findViewById(R.id.new_custom_stamp_guide_text_view);
        emptyTextView.setText(R.string.signature_new_guide);

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
                    && onBackPressed();
            }
        });

        mAttachSignatureTask = new AttachSignatureTask(view.getContext(), (ProgressBar) view.findViewById(R.id.progress_bar),
            emptyTextView, mSavedSignatureAdapter);
        mAttachSignatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAttachSignatureTask != null) {
            mAttachSignatureTask.cancel(true);
        }
    }

    public void setOnSavedSignatureListener(OnSavedSignatureListener listener) {
        mOnSavedSignatureListener = listener;
    }

    public void setToolbars(@NonNull Toolbar toolbar, @NonNull Toolbar cabToolbar) {
        mToolbar = toolbar;
        mCabToolbar = cabToolbar;
    }

    public void resetToolbar(final Context context) {
        if (mToolbar != null) {
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mToolbar == null || mCabToolbar == null) {
                        return false;
                    }

                    if (item.getItemId() == R.id.controls_action_edit) {
                        // Start edit-mode
                        mActionMode = new ToolbarActionMode(context, mCabToolbar);
                        mActionMode.startActionMode(mActionModeCallback);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private ToolbarActionMode.Callback mActionModeCallback = new ToolbarActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ToolbarActionMode mode, Menu menu) {
            mode.inflateMenu(R.menu.cab_fragment_saved_signature);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ToolbarActionMode mode, MenuItem item) {
            int id = item.getItemId();

            SparseBooleanArray selectedItems = mItemSelectionHelper.getCheckedItemPositions();
            int count = selectedItems.size();
            final List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < count; ++i) {
                if (selectedItems.valueAt(i)) {
                    indexes.add(selectedItems.keyAt(i));
                }
            }

            if (indexes.size() == 0) {
                return false;
            }

            if (id == R.id.controls_signature_action_delete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.signature_dialog_delete_message)
                    .setTitle(R.string.signature_dialog_delete_title)
                    .setPositiveButton(R.string.tools_misc_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // remove repeated indexes and then sort them in ascending order
                            Set<Integer> hs = new HashSet<>(indexes);
                            indexes.clear();
                            indexes.addAll(hs);
                            Collections.sort(indexes);

                            for (int i = indexes.size() - 1; i >= 0; --i) {
                                int index = indexes.get(i);
                                mSavedSignatureAdapter.removeAt(index);
                                mSavedSignatureAdapter.notifyItemRemoved(index);
                            }

                            clearSelectedList();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .create()
                    .show();
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ToolbarActionMode mode) {
            mActionMode = null;
            clearSelectedList();
        }

        @Override
        public boolean onPrepareActionMode(ToolbarActionMode mode, Menu menu) {
            if (Utils.isTablet(getContext()) || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mode.setTitle(getString(R.string.controls_thumbnails_view_selected,
                    Utils.getLocaleDigits(Integer.toString(mItemSelectionHelper.getCheckedItemCount()))));
            } else {
                mode.setTitle(Utils.getLocaleDigits(Integer.toString(mItemSelectionHelper.getCheckedItemCount())));
            }
            return true;
        }
    };

    private boolean finishActionMode() {
        boolean success = false;
        if (mActionMode != null) {
            success = true;
            mActionMode.finish();
            mActionMode = null;
        }
        clearSelectedList();
        return success;
    }

    private void clearSelectedList() {
        if (mItemSelectionHelper != null) {
            mItemSelectionHelper.clearChoices();
        }
        if (mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    private boolean onBackPressed() {
        if (!isAdded()) {
            return false;
        }

        boolean handled = false;
        if (mActionMode != null) {
            handled = finishActionMode();
        }
        return handled;
    }

    private static class AttachSignatureTask extends CustomAsyncTask<Void, Void, Bitmap[]> {

        WeakReference<ProgressBar> mProgressBarRef;
        WeakReference<TextView> mTextViewRef;
        WeakReference<SavedSignatureAdapter> mAdapterRef;

        AttachSignatureTask(Context context, ProgressBar progressBar, TextView textView,
                                   SavedSignatureAdapter adapter) {
            super(context);
            mProgressBarRef = new WeakReference<>(progressBar);
            mTextViewRef = new WeakReference<>(textView);
            mAdapterRef = new WeakReference<>(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressBar progressBar = mProgressBarRef.get();
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            File[] files = StampManager.getInstance().getSavedSignatures(getContext());
            if (files == null) {
                return null;
            }
            Bitmap[] bitmaps = new Bitmap[files.length];
            for (int i=0;i<files.length;i++) {
                File file = files[i];
                bitmaps[i] = StampManager.getInstance().getSavedSignatureBitmap(getContext(), file);
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);

            ProgressBar progressBar = mProgressBarRef.get();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            TextView emptyView = mTextViewRef.get();
            if (bitmaps == null || bitmaps.length == 0) {
                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            } else {
                if (emptyView != null) {
                    emptyView.setVisibility(View.GONE);
                }
                SavedSignatureAdapter adapter = mAdapterRef.get();
                if (adapter != null) {
                    adapter.setBitmaps(bitmaps);
                }
            }
        }
    }
}
