package sjes.elasticsearch.feigns.category.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by qinhailong on 15-10-20.
 */
@Data
public class NameValueModel implements Serializable {

    private String name;

    private String value;

}
