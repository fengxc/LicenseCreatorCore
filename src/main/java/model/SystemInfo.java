package model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemInfo {

    String cpuSerial;
    String mainBoardSerial;
    List<String> macAddressList;
}
