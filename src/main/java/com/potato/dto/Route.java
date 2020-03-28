package com.potato.dto;

import lombok.Data;

@Data
public class Route {

    private long airportID;
    private String name;//名称
    private String city;//城市
    private String country;//国家
    private String iATA;//国际航空运输协会
    private String iCAO;
    private Double latitude;//纬度
    private Double longitude;//经度
    private String altitude;
    private String timezone;
    private String dST;
    private String tzDatabaseTimeZone;
    private String type;
    private String source;


}
