package com.example.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {
    private final int port;
    private final Map<String, Object> services = Map.of(
            CalculatorService.class.getName(), new CalculatorServiceImpl());

    public RpcServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ExecutorService clients = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Mini-RPC server listening on port " + port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                clients.execute(() -> handle(socket));
            }
        } finally {
            clients.shutdownNow();
        }
    }

    private void handle(Socket socket) {
        try (socket;
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
            output.flush();
            Object value = input.readObject();
            if (!(value instanceof RpcRequest request)) {
                output.writeObject(RpcResponse.failure("Unsupported request type"));
                output.flush();
                return;
            }

            RpcResponse response;
            try {
                response = invoke(request);
            } catch (ReflectiveOperationException | RuntimeException e) {
                Throwable cause = e instanceof InvocationTargetException invocation
                        ? invocation.getTargetException() : e;
                response = RpcResponse.failure(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
            output.writeObject(response);
            output.flush();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to handle client: " + e.getMessage());
        }
    }

    private RpcResponse invoke(RpcRequest request) throws ReflectiveOperationException {
        Object service = services.get(request.getServiceName());
        if (service == null) {
            return RpcResponse.failure("Unknown service: " + request.getServiceName());
        }
        Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
        return RpcResponse.success(method.invoke(service, request.getArguments()));
    }

    public static void main(String[] args) throws IOException {
        new RpcServer(8888).start();
    }
}
