package ru.no3utuff4uk.converter;

import javafx.concurrent.Task;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Synchronized;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.no3utuff4uk.helper.Status;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PTEConverterImpl extends Task<Status> implements PTEConverter{
    /**
     * Файл исходного изображения
     */
    @Getter
    private File inputPicture;
    /**
     * Файл с результатом преобразования
     */
    @Getter
    private File outputFile;
    /**
     * Размер стороны ячейки
     */
    private Integer cellSize;

    public PTEConverterImpl(File inputPicture, File outputFile, Integer cellSize) {
        this.inputPicture = inputPicture;
        this.outputFile = outputFile;
        this.cellSize = cellSize;
    }

    @Override
    public void convert() throws IOException {
        BufferedImage image = ImageIO.read(inputPicture);

        XSSFWorkbook workbook = new XSSFWorkbook();
//       HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(inputPicture.getName());
        sheet.setDefaultColumnWidth(1);
        sheet.setDefaultRowHeight((short) 1);
        sheet.setZoom(10);
        HashMap<Integer, XSSFCellStyle> styles = new HashMap<>();
        AtomicLong progress = new AtomicLong(0);
        long maxProgress = image.getHeight() * image.getWidth();
        updateProgress(progress.longValue(), maxProgress);
        List<Row> rows = IntStream.range(0, image.getHeight()).mapToObj(y -> sheet.createRow(y)).collect(Collectors.toList());
        rows.parallelStream().forEach((Row row) -> {
//            long rowStart = System.currentTimeMillis();
            for (int x = 0; x < image.getWidth(); x++) {
                Cell cell = row.createCell(x);
                int color = image.getRGB(x, row.getRowNum()) | 0x000F0F0F;
                XSSFCellStyle style;
                if (!styles.containsKey(color)) {
                    style = workbook.createCellStyle();
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setFillForegroundColor(new XSSFColor(new Color(image.getRGB(x, row.getRowNum()))));
                    styles.put(color, style);
                } else {
                    style = styles.get(color);
                }
//                cell.setCellValue(Integer.toString(image.getRGB(x, y), 16));
                cell.setCellStyle(style);

                updateProgress(progress.incrementAndGet(), maxProgress);
//                System.out.println("kek " + progress);
            }
//            System.out.println("Row " + row.getRowNum() + " преобразована за " + ((double)(System.currentTimeMillis() - rowStart)/1000));
        });
        sheet.setActiveCell(new CellAddress("A1"));

        System.out.println("Преобразование завершено!");

//        for(int y = 0; y < image.getHeight(); y++) {
//            Row row = sheet.getRow(y);
//            for(int x = 0; x < image.getWidth(); x++) {
//                Cell cell = row.createCell(x);
//                XSSFCellStyle style;
//                if(!styles.containsKey(image.getRGB(x, y))) {
//                    style = workbook.createCellStyle();
//                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                    style.setFillForegroundColor(new XSSFColor(new Color(image.getRGB(x, y))));
//                    styles.put(image.getRGB(x, y), style);
//                } else {
//                    style = styles.get(image.getRGB(x, y));
//                }
////                cell.setCellValue(Integer.toString(image.getRGB(x, y), 16));
//                cell.setCellStyle(style);
//
//                updateProgress(++progress, maxProgress);
////                System.out.println("kek " + progress);
//            }
//        }
        @Cleanup
        FileOutputStream fos =new FileOutputStream(outputFile);
        workbook.write(fos);
    }

    @Override
    public Status call() throws Exception {
        try {
            convert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * Строитель для задачи конвертации изображения в Excel файл
     */
    public static class Builder {
        @Getter
        private File inputPicture;
        @Getter
        private File outputFile;
        @Getter
        private Integer cellSize = 50;

        public Builder setInputPicture(String path) {
            return setInputPicture(new File(path));
        }

        public Builder setInputPicture(File picture) {
            if(picture == null) {
                return this;
            }
            if(!picture.exists() || !picture.isFile()) {
                throw new IllegalArgumentException("Некорректный путь к изображению");
            }
            if(!picture.getName().matches("[^\\s]+(\\.(?i)(jpg|png|gif|bmp))$")) {
                throw new IllegalArgumentException("Файл не является изображением");
            }
            inputPicture = picture;
            return this;
        }

        public Builder setOutputFile(String path) {
            return setOutputFile(new File(path));
        }

        public Builder setOutputFile(File outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder setCellSize(Integer size) {
            if(size <= 0) {
                throw new IllegalArgumentException("Размер ячейки не может быть меньше или равным 0");
            }
            if(size > 100){
                throw new IllegalArgumentException("Размер ячейки не может быть больше, чем 100px");
            }
            cellSize = size;
            return this;
        }

        /**
         * Построение преобразователя по заданным параметрам
         * @return Преобразователь изображения в excel файл
         * @throws IllegalStateException
         */
        public PTEConverterImpl build() throws IllegalStateException {
            if(inputPicture == null) {
                throw new IllegalStateException("Не задано изображение");
            }
            if(outputFile == null) {
                throw new IllegalStateException("Не задан файл с результатом преобразования");
            }
            return new PTEConverterImpl(inputPicture, outputFile, cellSize);
        }
    }
}
