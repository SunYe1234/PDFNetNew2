package com.pdftron.collab.ui.reply.bottomsheet.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.bottomsheet.BottomSheetReplyFragment;
import com.pdftron.collab.ui.reply.component.input.BaseTextInputUIView;
import com.pdftron.pdf.utils.Utils;

/**
 * Base class for {@link BaseTextInputUIView} that represents a the text input field in the
 * {@link BottomSheetReplyFragment}
 * {@hide}
 */
public class ReplyInputUIView extends BaseTextInputUIView {

    private EditText mWriteMessageEditText;
    private EditText mEditMessageEditText;

    private final ViewGroup mParent;

    private final ConstraintLayout mWriteMessageComponent;
    private final ConstraintLayout mEditMessageComponent;

    public ReplyInputUIView(@NonNull ViewGroup parent) {
        super(parent);

        // Inflate views
        mParent = parent;
        Context context = mParent.getContext();
        mWriteMessageComponent = (ConstraintLayout) LayoutInflater.from(context)
                .inflate(R.layout.content_reply_message_write, mParent, false);
        mEditMessageComponent = (ConstraintLayout) LayoutInflater.from(context)
                .inflate(R.layout.content_reply_message_edit, mParent, false);

        // Setup views
        setupWriteMessage(mWriteMessageComponent);
        mParent.addView(mWriteMessageComponent); // we want to initially have the write message editor
        setupEditMessage(mEditMessageComponent);
    }

    private void setupWriteMessage(@NonNull ConstraintLayout editorContainer) {

        mWriteMessageEditText = editorContainer.findViewById(R.id.reply_editor);
        Button sendButton = editorContainer.findViewById(R.id.reply_send);

        mWriteMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ReplyInputUIView.this.onMessageWriteChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
        sendButton.setOnClickListener(v -> onMessageWriteFinished(mWriteMessageEditText.getText().toString()));
    }

    private void setupEditMessage(@NonNull ConstraintLayout editorContainer) {

        // Inflate views
        mEditMessageEditText = editorContainer.findViewById(R.id.reply_editor);
        Button enterButton = editorContainer.findViewById(R.id.reply_send);
        Button closeButton = editorContainer.findViewById(R.id.editor_close);

        mEditMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ReplyInputUIView.this.onMessageEditChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        // Handle send and close buttons
        enterButton.setOnClickListener(v -> onMessageEditFinished(mEditMessageEditText.getText().toString()));
        closeButton.setOnClickListener(v -> onMessagedEditCancelled());
    }

    @Override
    public void clearInput() {
        mWriteMessageEditText.setText("");
    }

    @Override
    public void showMessageEditView(String content) {
        // Show the keyboard when editing message
        mEditMessageEditText.requestFocus();
        Utils.showSoftKeyboard(mEditMessageEditText.getContext(), mEditMessageEditText);

        // Attach the edit message view and initialize the edit text
        mParent.removeAllViews();
        mParent.addView(mEditMessageComponent);
        mEditMessageEditText.setText("");
        mEditMessageEditText.append(content);
    }

    @Override
    public void closeMessageEditView() {
        // Hide the keyboard when finished editing message and clear the edit text
        mEditMessageEditText.setText("");
        Utils.hideSoftKeyboard(mEditMessageEditText.getContext(), mEditMessageEditText);
        mEditMessageEditText.clearFocus();

        // Detach the edit message view
        mParent.removeAllViews();
        mParent.addView(mWriteMessageComponent);
    }
}
