package data;

import model.Course;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<Course> readCSV(
            String filePath) {

        List<Course> courses =
                new ArrayList<>();

        String line;

        try {

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(filePath)
                    );

            // تخطي Header
           // br.readLine();
            // طباعة ال Header
            String header = br.readLine();
            System.out.println(header);

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty())
                    continue;

                String[] values =
                        line.split(",");

                // تأكد عدد الأعمدة
                if (values.length < 21) {

                    System.out.println(
                            "Skipped bad row: "
                                    + line);

                    continue;
                }

                //System.out.println(values.length);
                // print data
                /*for (int i = 0; i < values.length; i++) {
                    if (values[i].trim()=="X")
                        continue;

                    System.out.print(i + " : "+values[i]+" || ");
                }*/
                //System.out.println(values[3]);

                // استخراج البيانات
                String department =
                        values[0];

                String courseNumber =
                        values[1];

                String section =
                        values[2];

                String courseName =
                        values[3];

                String teacherName =
                        values[19];

                String roomCode =
                        values[17];

                // إنشاء Object
                Course course =
                        new Course(
                                department,
                                courseNumber,
                                section,
                                courseName,
                                teacherName,
                                roomCode
                        );

                //System.out.println(department+" "+courseNumber+" "+section+" "+" "+" "+ roomCode );
                // تخزينه
                courses.add(course);
            }

            br.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return courses;
    }
}