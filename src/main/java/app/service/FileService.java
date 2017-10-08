package app.service;

import app.controller.MainWindowController;
import javafx.scene.image.Image;
import lombok.NonNull;
import lombok.Synchronized;
import ru.no3utuff4uk.converter.PTEConverterImpl;

import java.io.File;
import java.io.FileInputStream;

public class FileService {

    public static Thread convertThread;

    public static void acceptPicture(@NonNull MainWindowController controller, File picture) {
        if(picture == null){
            return;
        }
        controller.pteConverterBuilder.setInputPicture(picture);

        controller.picturePathField.setText(controller.pteConverterBuilder.getInputPicture().getPath());
        try (FileInputStream pictureStream = new FileInputStream(picture)) {
            controller.previewField.setImage(new Image(pictureStream));
        } catch (Exception ex) {
            //TODO: Обработать ошибки
        }
        controller.outputFilePath.setDisable(false);
        controller.openOutputFileButton.setDisable(false);
        controller.cellSizeSpinner.setDisable(false);
    }

    public static void acceptOutputFile(@NonNull MainWindowController controller, File output) {
        if(output == null){
            return;
        }
        controller.pteConverterBuilder.setOutputFile(output);
        controller.outputFilePath.setText(controller.pteConverterBuilder.getOutputFile().getPath());

        controller.progressBar.setDisable(false);
        controller.startConvertButton.setDisable(false);
    }

    public static void startConvertation(MainWindowController controller) {
        PTEConverterImpl converter = controller.pteConverterBuilder.build();
        convertThread = new Thread(converter);
        controller.progressBar.progressProperty().unbind();
        controller.progressBar.progressProperty().bind(converter.progressProperty());

        convertThread.start();
    }
}
