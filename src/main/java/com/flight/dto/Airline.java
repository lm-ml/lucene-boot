package com.flight.dto;

import lombok.Data;

@Data
public class Airline {

    private long airlineId;
    private String name;
    private String alias;
    private String iATA;
    private String iCAO;
    private String callSign;
    private String country;
    private String active;

}
