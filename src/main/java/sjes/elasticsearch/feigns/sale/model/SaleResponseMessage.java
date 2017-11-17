package sjes.elasticsearch.feigns.sale.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by kimiyu on 16/12/11.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponseMessage<T> implements Serializable {

    private int code;

    private String message;

    private T data;

    public SaleResponseMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public SaleResponseMessage(T data) {
        this.data = data;
    }

    public static <T> SaleResponseMessage<T> responseResult(int code, String message, T data) {

        return data == null ? new SaleResponseMessage<T>(code, message) : new SaleResponseMessage<T>(code, message, data);
    }

    public static <T> SaleResponseMessage<T> failure(int code, String message) {
        return new SaleResponseMessage<>(code, message);
    }

    public static <T> SaleResponseMessage<T> success(T data) {
        return new SaleResponseMessage<>(1,"success", data);
    }

}
