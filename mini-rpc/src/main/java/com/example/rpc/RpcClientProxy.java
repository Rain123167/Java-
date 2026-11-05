package com.example.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RpcClientProxy implements InvocationHandler {
    private final String host;
    private final int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static <T> T create(Class<T> serviceInterface, String host, int port) {
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException("RPC service type must be an interface");
        }
        Object proxy = Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new RpcClientProxy(host, port));
        return serviceInterface.cast(proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> "RpcProxy(" + method.getDeclaringClass().getName() + ")";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> method.invoke(this, arguments);
            };
        }

        RpcRequest request = new RpcRequest(
                method.getDeclaringClass().getName(),
                method.getName(),
                method.getParameterTypes(),
                arguments);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5_000);
            socket.setSoTimeout(5_000);
            try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                output.writeObject(request);
                output.flush();
                try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                    Object value = input.readObject();
                    if (!(value instanceof RpcResponse response)) {
                        throw new RpcException("Invalid response from RPC server");
                    }
                    if (!response.isSuccess()) {
                        throw new RpcException("Remote call failed: " + response.getError());
                    }
                    return response.getResult();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("RPC call to " + host + ":" + port + " failed", e);
        }
    }

    public static class RpcException extends RuntimeException {
        public RpcException(String message) { super(message); }
        public RpcException(String message, Throwable cause) { super(message, cause); }
    }
}
