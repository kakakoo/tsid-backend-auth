package com.tsid.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResValue<T> {

    private boolean status;
    private T data;
    private ErrorResponse error;

    @Builder
    public ResValue(T data){
        this.data = data;
        this.status = true;
    }

    @Builder
    public ResValue(ErrorResponse error){
        this.error = error;
        this.status = false;
    }
}
