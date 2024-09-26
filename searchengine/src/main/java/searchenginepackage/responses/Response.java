package searchenginepackage.responses;

import lombok.Data;

import java.util.*;

@Data
public class Response {
    private boolean result;
    private String message;
    public Response() {
        result = true;
    }
    public Response(String message) {
        result = false;
        this.message = message;
        System.out.println(message);
    }

}
