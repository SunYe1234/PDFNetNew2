package com.pdftron.collab.ui.reply.component;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.db.entity.ReplyEntity;
import com.pdftron.collab.ui.reply.model.ReplyMessage;
import com.pdftron.collab.ui.reply.model.ReplyMessageContent;
import com.pdftron.collab.ui.reply.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter that converts {@link ReplyEntity} objects to {@link ReplyMessage} objects.
 */
public class ReplyEntityMapper {

    /**
     * Converts a list of {@link ReplyEntity} into a list of {@link ReplyMessage}.
     *
     * @param replyEntities to convert
     * @param currentUser   used to determine if a {@link ReplyEntity} is editable by the user.
     * @return list of {@link ReplyMessage}
     */
    public static List<ReplyMessage> fromEntities(List<ReplyEntity> replyEntities, User currentUser) {
        List<ReplyMessage> messages = new ArrayList<>();
        for (ReplyEntity entity : replyEntities) {
            messages.add(fromEntityImpl(entity, currentUser));
        }
        return messages;
    }

    private static ReplyMessage fromEntityImpl(ReplyEntity replyEntity, User currentUser) {
        String authorId = replyEntity.getAuthorId();
        String authorName = replyEntity.getAuthorName();
        String contents = replyEntity.getContents();
        Date creationDate = replyEntity.getCreationDate();
        String id = replyEntity.getId();
        String inReplyTo = replyEntity.getInReplyTo();
        String iconTxt = getInitials(authorName);
        int page = replyEntity.getPage();
        boolean isEditable = currentUser.getId().equals(authorId);

        return new ReplyMessage(
                id,
                new User(authorId, authorName),
                new ReplyMessageContent(contents),
                creationDate,
                iconTxt,
                page,
                isEditable
        );
    }

    @NonNull
    public static String getInitials(@Nullable String str) {
        if (str == null || str.length() == 0) {
            return "";
        } else if (str.length() == 1) {
            return str;
        } else {
            String[] words = str.trim().split(" ");
            if (words.length == 1) {
                return words[0].substring(0, 1);
            } else {
                return words[0].substring(0, 1) + words[1].substring(0, 1);
            }
        }
    }
}
