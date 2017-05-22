package sjes.elasticsearch.controller;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchParam<T> implements Serializable {

    private Integer page;

    private Integer size = 10;

    private T data;
}
