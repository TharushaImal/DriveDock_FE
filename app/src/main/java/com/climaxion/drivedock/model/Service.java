package com.climaxion.drivedock.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Service implements Serializable {

    private int id;
    private String name;
    private String description;
    private double price;
}
