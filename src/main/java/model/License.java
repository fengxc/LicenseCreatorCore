package model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class License {

    public enum LicenseType {
        //正式版：起止日期按约定，按MAC校验
        GA("GA", "正式"),
        //试用版：结束日期按约定，最长不超过6个月，超限宽限8小时，可不限制Mac
        TRAIL("TRAIL", "试用"),
        //开发版：结束日期为8小时，不限制Mac，不限制模块
        DEV("DEV", "开发"),
        //不限制MAC，限制日期，但超期可以演示168小时
        DEMO("DEMO", "演示");

        private String type;
        private String typeName;

        private LicenseType(String type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        public String getType() {
            return type;
        }

        public String getTypeName() {
            return typeName;
        }

        @Override
        public String toString() {
            return "LicenseType{" +
                    "type='" + type + '\'' +
                    ", typeName='" + typeName + '\'' +
                    '}';
        }
    };

    private String description;

    private String cpuSerial;

    private String mainBoardSerial;

    private List<String> macAddress;

    private String validMode;

    private Date startDate;

    private Date endDate;

    private Date createDate;

    private LicenseType type;

    private List<String> module;
}
