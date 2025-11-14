package org.hfut.work.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code; // 0 success, non-zero error codes
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 0;
        r.message = "OK";
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static ApiResponse<Void> ok() {
        ApiResponse<Void> r = new ApiResponse<>();
        r.code = 0;
        r.message = "OK";
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        r.timestamp = System.currentTimeMillis();
        return r;
    }
}


