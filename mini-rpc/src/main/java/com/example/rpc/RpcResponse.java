package com.example.rpc;

import java.io.Serial;
import java.io.Serializable;

public class RpcResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Object result;
    private final String error;

    private RpcResponse(Object result, String error) {
        this.result = result;
        this.error = error;
    }

    public static RpcResponse success(Object result) { return new RpcResponse(result, null); }
    public static RpcResponse failure(String error) { return new RpcResponse(null, error); }
    public Object getResult() { return result; }
    public String getError() { return error; }
    public boolean isSuccess() { return error == null; }
}
