package data;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

public class ExcelReader {

    public static void readExcel(
            String filePath) {

        try {

            FileInputStream file =
                    new FileInputStream(filePath);

            Workbook workbook =
                    new XSSFWorkbook(file);

            Sheet sheet =
                    workbook.getSheetAt(0);

            for (Row row : sheet) {

                // تخطي Header
                if (row.getRowNum() == 0)
                    continue;

                Cell courseCodeCell =
                        row.getCell(0);

                Cell courseNumberCell =
                        row.getCell(1);

                Cell courseNameCell =
                        row.getCell(3);

                Cell teacherCell =
                        row.getCell(14);

                String courseCode =
                        courseCodeCell
                                .toString();

                String courseNumber =
                        courseNumberCell
                                .toString();

                String courseName =
                        courseNameCell
                                .toString();

                String teacherName =
                        teacherCell
                                .toString();

                System.out.println(
                        courseCode
                                + " - "
                                + courseNumber
                                + " - "
                                + courseName
                                + " - "
                                + teacherName
                );
            }

            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}