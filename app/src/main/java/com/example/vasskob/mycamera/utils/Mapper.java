package com.example.vasskob.mycamera.utils;

public interface Mapper<FROM, TO> {
    TO map(FROM data);
}
