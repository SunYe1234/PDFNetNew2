package com.pdftron.pdf.dialog.pagelabel;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

import java.util.TreeMap;

class PageLabelComponent implements PageLabelView.PageLabelSettingChangeListener {

    private final PageLabelView mPageLabelDialogView;
    private final PageLabelSettingViewModel mPageLabelViewModel;
    private final DialogButtonInteractionListener mListener;

    PageLabelComponent(@NonNull FragmentActivity activity, @NonNull ViewGroup parent,
                       int selectedPage, int numPages,
                       @NonNull DialogButtonInteractionListener listener) {

        this(activity, parent, new PageLabelSetting(selectedPage, numPages), listener);
    }

    PageLabelComponent(@NonNull FragmentActivity activity,
                       @NonNull ViewGroup parent,
                       int fromPage,
                       int toPage,
                       int numPages,
                       @NonNull DialogButtonInteractionListener listener) {

        this(activity, parent, new PageLabelSetting(fromPage, toPage, numPages), listener);
    }

    PageLabelComponent(@NonNull FragmentActivity activity,
                       @NonNull ViewGroup parent,
                       int fromPage,
                       int toPage,
                       int numPages,
                       @NonNull String prefix,
                       @NonNull DialogButtonInteractionListener listener) {

        this(activity, parent, new PageLabelSetting(fromPage, toPage, numPages, prefix), listener);
    }

    private PageLabelComponent(@NonNull FragmentActivity activity,
                       @NonNull ViewGroup parent,
                       @NonNull PageLabelSetting pageLabelSetting,
                       @NonNull DialogButtonInteractionListener listener) {

        mListener = listener;
        mPageLabelViewModel = ViewModelProviders.of(activity).get(PageLabelSettingViewModel.class);
        mPageLabelViewModel.set(pageLabelSetting);
        mPageLabelDialogView = new PageLabelDialogView(parent, this);
        mPageLabelDialogView.initViewStates(pageLabelSetting);
    }

    @Override
    public void setAll(boolean isAll) {
        mPageLabelViewModel.get().setAll(isAll);
        updateViewValidity();
    }

    @Override
    public void setSelectedPage(boolean isSelected) {
        mPageLabelViewModel.get().setSelectedPage(isSelected);
        updateViewValidity();
    }

    @Override
    public void setPageRange(@NonNull String fromText, @NonNull String toText) {
        try {
            int from = Integer.valueOf(fromText);
            int to = Integer.valueOf(toText);
            mPageLabelViewModel.get().setToPage(to);
            mPageLabelViewModel.get().setFromPage(from);
            updateViewValidity();
        } catch (NumberFormatException e) {
            mListener.disallowSave();
            mPageLabelDialogView.invalidToPage(false);
            mPageLabelDialogView.invalidFromPage(false);
        }
    }

    @Override
    public void setStyle(@NonNull PageLabelSetting.PageLabelStyle style) {
        mPageLabelViewModel.get().setStyle(style);
        updatePreview();
        updateViewValidity();
    }

    @Override
    public void setPrefix(@NonNull String prefix) {
        mPageLabelViewModel.get().setPrefix(prefix);
        updatePreview();
    }

    @Override
    public void setStartNumber(@NonNull String startStr) {

        try {
            int start = Integer.valueOf(startStr);
            mPageLabelViewModel.get().setStartNum(start);
            updateViewValidity();
            updatePreview();
        } catch (NumberFormatException e) {
            mListener.disallowSave();
            mPageLabelDialogView.invalidStartNumber(false);
        }
    }

    @Override
    public void completeSettings() {
        int fromPage, toPage;
        if (mPageLabelViewModel.get().isAll()) {
            fromPage = 1;
            toPage = mPageLabelViewModel.get().numPages;
        } else if (mPageLabelViewModel.get().isSelectedPage()) {
            fromPage = mPageLabelViewModel.get().selectedPage;
            toPage = mPageLabelViewModel.get().selectedPage;
        } else {
            fromPage = mPageLabelViewModel.get().getFromPage();
            toPage = mPageLabelViewModel.get().getToPage();
        }
        // Set valid data so that the caller can use the proper parameters
        mPageLabelViewModel.get().setFromPage(fromPage);
        mPageLabelViewModel.get().setToPage(toPage);
        // Notify caller and clear page label settings and the view
        mPageLabelViewModel.complete();
    }

    private void updatePreview() {
        String preview = "";
        PageLabelSetting.PageLabelStyle style = mPageLabelViewModel.get().getStyle();
        String prefix = mPageLabelViewModel.get().getPrefix();
        int startNum = mPageLabelViewModel.get().getStartNum();
        switch (style) {
            case NONE:
                preview = String.format("%1$s, %1$s, %1$s, ...", prefix);
                break;
            case ROMAN_UPPER:
                preview = String.format(getRomanPreviewFormat(startNum, false), prefix);
                break;
            case ROMAN_LOWER:
                preview = String.format(getRomanPreviewFormat(startNum, true), prefix);
                break;
            case ALPHA_UPPER:
                preview = String.format(getAlphabeticPreviewFormat(startNum, false), prefix);
                break;
            case ALPHA_LOWER:
                preview = String.format(getAlphabeticPreviewFormat(startNum, true), prefix);
                break;
            case DECIMAL:
                preview = String.format("%1$s%2$d, %1$s%3$d, %1$s%4$d, ...", prefix, startNum, startNum + 1, startNum + 2);
                break;
        }

        mPageLabelDialogView.updatePreview(preview);
    }

    private void updateViewValidity() {
        int numPages = mPageLabelViewModel.get().numPages;
        int to = mPageLabelViewModel.get().getToPage();
        int from = mPageLabelViewModel.get().getFromPage();
        int startNum = mPageLabelViewModel.get().getStartNum();

        // Check if from and to page is correct
        boolean isFromPageCorrect =
                mPageLabelViewModel.get().isAll() || mPageLabelViewModel.get().isSelectedPage()
                        || (from <= to && from >= 1 && from <= numPages);
        boolean isToPageCorrect =
                mPageLabelViewModel.get().isAll() || mPageLabelViewModel.get().isSelectedPage()
                        || (from <= to && to >= 1 && to <= numPages);

        mPageLabelDialogView.invalidToPage(isToPageCorrect);
        mPageLabelDialogView.invalidFromPage(isFromPageCorrect);

        // Check if starting number is valid
        boolean isStartingNumberValid = mPageLabelViewModel.get().getStyle() == PageLabelSetting.PageLabelStyle.NONE
                || startNum >= 1;
        if (isStartingNumberValid) {
            mPageLabelDialogView.invalidStartNumber(true);
        } else {
            mPageLabelDialogView.invalidStartNumber(false);
        }

        // Update the complete button state
        if (isFromPageCorrect && isToPageCorrect && isStartingNumberValid) {
            mListener.allowSave();
        } else {
            mListener.disallowSave();
        }
    }

    private String getRomanPreviewFormat(int startNum, boolean isLowercase) {
        String first = toLowerCase(PageLabelNumber.toRoman(startNum), isLowercase);
        String second = toLowerCase(PageLabelNumber.toRoman(startNum + 1), isLowercase);
        String third = toLowerCase(PageLabelNumber.toRoman(startNum + 2), isLowercase);

        return "%1$s" + first + ", " + "%1$s" + second + ", " + "%1$s" + third + ", ...";
    }

    private String getAlphabeticPreviewFormat(int startNum, boolean isLowercase) {
        String first = toLowerCase(PageLabelNumber.toAlphabetic(startNum), isLowercase);
        String second = toLowerCase(PageLabelNumber.toAlphabetic(startNum + 1), isLowercase);
        String third = toLowerCase(PageLabelNumber.toAlphabetic(startNum + 2), isLowercase);

        return "%1$s" + first + ", " + "%1$s" + second + ", " + "%1$s" + third + ", ...";
    }

    /**
     * Converts all characters in str to lowercase if shouldLowercase is true.
     * Converts all characters to uppercase otherwise.
     *
     * @param str             String to change casing
     * @param shouldLowercase true if we want to set str to lowercase, false to set uppercase
     * @return string with converted characters to the defined casing
     */
    private String toLowerCase(String str, boolean shouldLowercase) {
        return shouldLowercase ? str.toLowerCase() : str.toUpperCase();
    }

    /**
     * Helper class to convert from integer to roman numerals.
     * <p>
     * from https://stackoverflow.com/a/19759564
     */
    private static class PageLabelNumber {
        private final static TreeMap<Integer, String> map = new TreeMap<>();
        static char[] alpha = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        static {
            map.put(1000, "M");
            map.put(900, "CM");
            map.put(500, "D");
            map.put(400, "CD");
            map.put(100, "C");
            map.put(90, "XC");
            map.put(50, "L");
            map.put(40, "XL");
            map.put(10, "X");
            map.put(9, "IX");
            map.put(5, "V");
            map.put(4, "IV");
            map.put(1, "I");
        }

        static String toRoman(int number) {
            if (number > 40000) { // We should cap this at some point, 30000 seems good enough. Any larger wont fit on screen anyways
                return "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM";
            } else {
                int l = map.floorKey(number);
                if (number == l) {
                    return map.get(number);
                }
                return map.get(l) + toRoman(number - l);
            }
        }

        static String toAlphabetic(int startNum) {
            if (startNum > 0) {
                int times2Repeat = (startNum - 1) / 26 + 1;
                int pos = (startNum - 1) % 26;
                String letter = String.valueOf(alpha[pos]);
                StringBuilder result = new StringBuilder();
                while (times2Repeat > 0) {
                    result.append(letter);
                    times2Repeat--;
                }
                return result.toString();
            } else {
                return "";
            }
        }
    }

    interface DialogButtonInteractionListener {

        void disallowSave();

        void allowSave();
    }
}
