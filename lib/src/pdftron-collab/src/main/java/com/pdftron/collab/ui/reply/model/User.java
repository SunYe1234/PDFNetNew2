package com.pdftron.collab.ui.reply.model;

import java.util.Objects;

/**
 * View state model representing a user.
 */
public class User {
    private final String id;
    private final String userName;

    public User(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    /**
     * @return username of this user. Displayed by the UI.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return id of the user.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName);
    }
}
