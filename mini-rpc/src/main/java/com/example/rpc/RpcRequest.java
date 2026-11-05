package com.example.rpc;

import java.io.Serial;
import java.io.Serializable;

public class RpcRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] arguments;

    public RpcRequest(String serviceName, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes.clone();
        this.arguments = arguments == null ? new Object[0] : arguments.clone();
    }

    public String getServiceName() { return serviceName; }
    public String getMethodName() { return methodName; }
    public Class<?>[] getParameterTypes() { return parameterTypes.clone(); }
    public Object[] getArguments() { return arguments.clone(); }
}
