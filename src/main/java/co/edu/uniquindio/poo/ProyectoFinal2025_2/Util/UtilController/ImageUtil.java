package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.InputStream;

/**
 * Utility class for common image-related operations such as
 * safe loading and circular clipping.
 */
public class ImageUtil {

    /**
     * Loads an image safely from the classpath.
     *
     * @param path resource path
     * @return loaded Image or null if not found
     */
    public static Image safeLoad(String path) {
        try (InputStream stream = ImageUtil.class.getResourceAsStream(path)) {
            if (stream == null) return null;
            Image image = new Image(stream);
            return image.isError() ? null : image;
        } catch (Exception e) {
            Logger.error("[ImageUtil] Failed to load image: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads an image from the file system (absolute path).
     * Use this for user-uploaded images stored on disk.
     *
     * @param filePath absolute file system path
     * @return loaded Image or null if not found/invalid
     */
    public static Image loadFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                Logger.warn("[ImageUtil] File does not exist: " + filePath);
                return null;
            }

            Image image = new Image(file.toURI().toString());
            if (image.isError()) {
                Logger.error("[ImageUtil] Error loading image from: " + filePath);
                return null;
            }

            return image;
        } catch (Exception e) {
            Logger.error("[ImageUtil] Failed to load image from file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads the default profile image from a given path.
     *
     * @param path the resource path
     * @return loaded Image or null
     */
    public static Image loadDefaultImage(String path) {
        Image img = safeLoad(path);
        if (img == null) Logger.warn("[ImageUtil] Default image not found at " + path);
        return img;
    }

    /**
     * Applies a circular clip to the specified ImageView.
     *
     * @param imageView target ImageView
     * @param radius    clip radius
     */
    public static void applyCircularClip(ImageView imageView, double radius) {
        imageView.setClip(new Circle(radius, radius, radius));
    }
}
