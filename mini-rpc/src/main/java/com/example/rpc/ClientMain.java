package com.example.rpc;

public class ClientMain {
    public static void main(String[] args) {
        CalculatorService calculator = RpcClientProxy.create(CalculatorService.class, "localhost", 8888);
        int result = calculator.add(10, 20);
        System.out.println("10 + 20 = " + result);
    }
}
