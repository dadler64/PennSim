package com.pennsim.util;

import com.pennsim.util.LocaleSelector.LocaleOption;
import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



class LocaleSelector extends JList<LocaleOption> implements LocaleListener, ListSelectionListener {

    private final LocaleOption[] localeOptions;

    LocaleSelector(Locale[] locales) {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultListModel<LocaleOption> model = new DefaultListModel<>();
        localeOptions = new LocaleOption[locales.length];
        for (int i = 0; i < locales.length; i++) {
            localeOptions[i] = new LocaleOption(locales[i]);
            model.addElement(localeOptions[i]);
        }
        setModel(model);
        setVisibleRowCount(Math.min(localeOptions.length, 8));
        LocaleManager.addLocaleListener(this);
        localeChanged();
        addListSelectionListener(this);
    }

    public void localeChanged() {
        Locale current = LocaleManager.getLocale();
        LocaleOption selectedOption = null;
        for (LocaleOption localeOption : localeOptions) {
            localeOption.update(current);
            if (current.equals(localeOption.locale)) {
                selectedOption = localeOption;
            }
        }
        if (selectedOption != null) {
            setSelectedValue(selectedOption, true);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        LocaleOption localeOption = getSelectedValue();
        if (localeOption != null) {
            SwingUtilities.invokeLater(localeOption);
        }
    }

    static class LocaleOption implements Runnable {

        private final Locale locale;
        private String text;

        LocaleOption(Locale locale) {
            this.locale = locale;
            update(locale);
        }

        @Override
        public String toString() {
            return text;
        }

        void update(Locale current) {
            if (current != null && current.equals(locale)) {
                text = locale.getDisplayName(locale);
            } else {
                text = locale.getDisplayName(locale) + " / " +
                        (current != null ? locale.getDisplayName(current) : "LOCALE_SELECTOR_ERROR");
            }
        }

        public void run() {
            if (!LocaleManager.getLocale().equals(locale)) {
                LocaleManager.setLocale(locale);
//                AppPreferences.LOCALE.set(locale.getLanguage());
            }
        }
    }
}
