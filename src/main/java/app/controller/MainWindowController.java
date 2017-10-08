package app.controller;

import app.service.FileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import ru.no3utuff4uk.converter.PTEConverterImpl;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainWindowController implements Initializable {
    @FXML
    public TextField picturePathField;
    @FXML
    public TextField outputFilePath;
    @FXML
    public Button openOutputFileButton;
    @FXML
    public Spinner<Integer> cellSizeSpinner;
    @FXML
    public ImageView previewField;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button startConvertButton;

    public FileChooser pictureFileChooser;
    public FileChooser outputPathChooser;
    public PTEConverterImpl.Builder pteConverterBuilder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pteConverterBuilder = new PTEConverterImpl.Builder();
        cellSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, pteConverterBuilder.getCellSize()));

        pictureFileChooser = new FileChooser();
        pictureFileChooser.setTitle("Выберите файл изображения");
        pictureFileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Pictures", "*.jpg", "*.jpeg", "*.png"));

        outputPathChooser = new FileChooser();
        outputPathChooser.setTitle("Выберите куда сохранить файл");
        outputPathChooser.setInitialFileName("output");
        outputPathChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Excel document", "*.xlsx"));
    }

    public void openPictureFile(ActionEvent actionEvent) {
        File picture = pictureFileChooser.showOpenDialog(((Node)actionEvent.getTarget()).getScene().getWindow());
        FileService.acceptPicture(this, picture);
    }

    public void acceptDropFile(DragEvent dragEvent) {
        boolean success = false;
        if (dragEvent.getDragboard().hasFiles()) {
            FileService.acceptPicture(this, dragEvent.getDragboard().getFiles().get(0));
            success = true;
        }
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }

    public void checkDropFileExtension(DragEvent dragEvent) {
        List<String> extensions = pictureFileChooser.getExtensionFilters().get(0).getExtensions();
        if (dragEvent.getDragboard().hasFiles() && dragEvent.getDragboard().getFiles().size() == 1) {
            if (!extensions.containsAll(
                    dragEvent.getDragboard().getFiles().stream()
                            .map(file -> {
                                String fileName = file.getName();
                                return "*." + fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                            })
                            .collect(Collectors.toList()))) {

                dragEvent.consume();
                return;
            }
            dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        dragEvent.consume();
    }

    public void createOutputFile(ActionEvent actionEvent) {
        File output = outputPathChooser.showSaveDialog(((Node)actionEvent.getTarget()).getScene().getWindow());
        FileService.acceptOutputFile(this, output);
    }

    public void startConvertation(ActionEvent actionEvent) {
        FileService.startConvertation(this);
    }
}
