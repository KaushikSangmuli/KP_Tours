package KP_TOURS.util;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class SvgIconUtil {

    public static Node loadIcon(String path, double size) {
        Image image = new Image(
                SvgIconUtil.class.getResourceAsStream(path),
                size, size, true, true
        );

        ImageView iv = new ImageView(image);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        return iv;
    }
}