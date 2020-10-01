package com.pennsim.gui.generic;

import com.pennsim.util.WindowClosable;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class PFrame extends JFrame implements WindowClosable {

    private static final String PATH = "pennsim/img/icons-";
    private static final int[] SIZES = {16, 20, 24, 48, 64, 128};
    private static final int DEFAULT_SIZE = 48;
    private static List<Image> ICONS = null;
    private static Image DEFAULT_ICON = null;

    public PFrame(String title) {
        super(title);
        PFrame.attachIcon(this);
    }

    public static void attachIcon(Window frame) {
        if (ICONS == null) {
            List<Image> loadedIcons = new ArrayList<>();
            ClassLoader loader = PFrame.class.getClassLoader();
            for (int size : SIZES) {
                URL url = loader.getResource(PATH + size + ".png");
                if (url != null) {
                    ImageIcon icon = new ImageIcon(url);
                    loadedIcons.add(icon.getImage());
                    if (size == DEFAULT_SIZE) {
                        DEFAULT_ICON = icon.getImage();
                    }
                }
            }
            ICONS = loadedIcons;
        }

        boolean success = false;
        try {
            if (ICONS != null && !ICONS.isEmpty()) {
                Method set = frame.getClass().getMethod("setIconImages", List.class);
                set.invoke(frame, ICONS);
                success = true;
            }
        } catch (Exception e) {
        }

        if (!success && frame instanceof JFrame && DEFAULT_ICON != null) {
            frame.setIconImage(DEFAULT_ICON);
        }
    }

    public void requestClose() {
        WindowEvent closing = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        processWindowEvent(closing);
    }
}