package pos.ui;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Simple broadcaster to notify attached MainLayout instances to refresh their drawer.
 */
public class Broadcaster {
    private static final CopyOnWriteArraySet<MainLayout> subscribers = new CopyOnWriteArraySet<>();

    public static void register(MainLayout ml) {
        subscribers.add(ml);
    }

    public static void unregister(MainLayout ml) {
        subscribers.remove(ml);
    }

    public static void broadcast() {
        for (MainLayout ml : subscribers) {
            try {
                ml.refreshDrawerAsync();
            } catch (Exception ignored) {
            }
        }
    }
}
