package com.pdftron.collab.ui.reply.component.input;

import android.support.annotation.Nullable;

interface TextInputInteraction {

    void onMessageWriteChanged(@Nullable String input);

    void onMessageWriteFinished(@Nullable String input);

    void onMessageEditChanged(@Nullable String input);

    void onMessageEditFinished(@Nullable String newMessage);

    void onMessagedEditCancelled();
}
