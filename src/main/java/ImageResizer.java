import javafx.scene.image.Image;

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageResizer {
    public static Image resizeImageInMemory(String inputImagePath, double maxWidth, double maxHeight) throws IOException {
        File inputFile = new File(inputImagePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputFile)
                .size((int) maxWidth, (int) maxHeight)
                .keepAspectRatio(true)
                .toOutputStream(byteArrayOutputStream);

        byte[] imageData = byteArrayOutputStream.toByteArray();
        return new Image(new java.io.ByteArrayInputStream(imageData));
    }
}
