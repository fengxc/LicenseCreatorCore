package test;

import model.License;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExampleLicense {
    String [] modules= {"sys","biz","auth","dev","gen","mobile","pm","sp","sec","mt","cm","flow","netdisk","rdm","train"};

    public License getExample() {
        License license = new License();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            license.setStartDate(df.parse("2025-01-01"));
            license.setEndDate(df.parse("2025-05-31"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        license.setDescription("Example license");
        List<String> list = List.of(modules);

        license.setModule(list);
        return license;
    }
}
