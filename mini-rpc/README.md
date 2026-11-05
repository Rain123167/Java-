# Mini-RPC

A small RPC learning project using JDK 17, Maven, Java sockets, Java native serialization, reflection, and a JDK dynamic proxy. The client calls `CalculatorService.add(10, 20)`; the server performs the call and the client prints `30`.

## Project structure

```text
src/main/java/com/example/rpc/
├── RpcRequest.java           # Serialized method call
├── RpcResponse.java          # Serialized result or remote error
├── CalculatorService.java    # Remote service interface
├── CalculatorServiceImpl.java
├── RpcServer.java            # Socket server and reflective dispatcher
├── RpcClientProxy.java       # Dynamic proxy and socket transport
└── ClientMain.java           # Example client
```

## Requirements

- JDK 17+
- Maven 3.8+

## Run

Open two terminals in this directory.

Terminal 1, start the server:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.example.rpc.RpcServer
```

Terminal 2, run the client:

```bash
mvn -q exec:java -Dexec.mainClass=com.example.rpc.ClientMain
```

The server listens on port **8888**. Expected client output:

```text
10 + 20 = 30
```

Stop the server with Ctrl+C.

## Request flow

1. The client proxy captures the interface name, method name, parameter types, and arguments.
2. It serializes an `RpcRequest` and sends it over a TCP socket.
3. The server deserializes the request, finds the registered implementation, and invokes the method with reflection.
4. The server sends an `RpcResponse`; the proxy returns its result to the caller.

## Learning note

This example uses Java native serialization for clarity. Do not deserialize data from untrusted network clients in production. Production RPC systems also need authentication, timeouts, connection management, versioned wire formats, and bounded server concurrency.
