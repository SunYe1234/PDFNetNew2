package com.pdftron.pdf.interfaces.builder;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Classes that implement this class can build objects of type {@link E}.
 *
 * @param <E> Type of fragment that will be built by this builder.
 */
public interface Builder<E> {

    /**
     * Create the specified object of type {@link E}, initialized with builder settings.
     *
     * @param context     the context used to initialize the fragment and its theme.
     * @param clazz the class this builder will use to create the instance.
     * @param <T>         the type parameter for the {@link E}.
     * @return a {@link E} that is an instance of the given type {@link T},
     * with the specified parameters from the builder.
     */
    <T extends E> T build(@NonNull Context context, @NonNull Class<T> clazz);
}
