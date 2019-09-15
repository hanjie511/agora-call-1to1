package com.example.hj.call;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
