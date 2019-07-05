package com.pdftron.pdf.interfaces.builder;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Skeletal class with minimal implementation required for a Fragment builder.
 */
public abstract class SkeletalFragmentBuilder<E extends Fragment> implements Builder<E>, Parcelable {

    /**
     * Create the specified Fragment of type {@link E}, initialized with builder settings.
     * Uses the theme from the specified context.
     *
     * @param context the context used to initialize the fragment and its theme.
     * @param clazz   the class this builder will use to create the instance.
     * @param <T>     the type parameter for the {@link E}.
     * @return a {@link E} that is an instance of the given type {@link T},
     * with the specified parameters from the builder.
     */
    @Override
    public <T extends E> T build(@NonNull Context context, @NonNull Class<T> clazz) {
        checkArgs(context);
        //noinspection unchecked
        return (T) Fragment.instantiate(context, clazz.getName(), createBundle(context));
    }

    /**
     * Convenience build method that calls {@link #build(Context, Class)} with the default class
     * for this builder.
     *
     * @param context the context used to initialize the fragment and its theme.
     * @return an instance of {@link E} with the specified parameters from the builder.
     */
    public E build(@NonNull Context context) {
      return null;
    }

    /**
     * Create the bundle that will be passed as arguments to the Fragment. Called right after
     * calling {@link #checkArgs(Context)}
     *
     * @param context used to initialize arguments for the bundle.
     * @return the bundle with the required arguments for the Fragment.
     */
    public abstract Bundle createBundle(@NonNull Context context);

    /**
     * Check the arguments of this builder. You can also det the default builder parameters in this
     * method.
     *
     * @param context used to initialize default building parameters
     */
    public abstract void checkArgs(@NonNull Context context);
}
