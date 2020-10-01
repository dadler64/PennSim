package com.pennsim.gui.start;

import com.pennsim.gui.GUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;

class MacOsAdapter { //MAC extends ApplicationAdapter {
    private GUI gui;

    static void addListeners(boolean added) {
        MyListener myListener = new MyListener();
        if (!added) {
            MRJAdapter.addOpenDocumentListener(myListener);
        }
        if (!added) {
            MRJAdapter.addPrintDocumentListener(myListener);
        }
        MRJAdapter.addPreferencesListener(myListener);
        MRJAdapter.addQuitApplicationListener(myListener);
        MRJAdapter.addAboutListener(myListener);
    }

    public static void register() {
    }

    private static class MyListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            ApplicationEvent applicationEvent = (ApplicationEvent) actionEvent;
            int type = applicationEvent.getType();
            switch (type) {
                //TODO Implement about menu
                case ApplicationEvent.ABOUT:
                    System.out.println("IMPLEMENT ABOUT PANEL!");
                    break;
                //TODO Implement proper closing procedures
                case ApplicationEvent.QUIT_APPLICATION:
                    System.exit(1);
                    break;
                case ApplicationEvent.OPEN_DOCUMENT:
                    Startup.doOpen(applicationEvent.getFile());
                    break;
                //TODO implement preference pane
                case ApplicationEvent.PREFERENCES:
                    System.out.println("IMPLEMENT PREFERENCES PANEL!");
                    break;
            }
        }
    }
}
