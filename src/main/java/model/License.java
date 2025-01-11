package model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class License {

    private String description;

    private List<String> macAddress;

    private Date startDate;

    private Date endDate;

    private List<String> module;
}
