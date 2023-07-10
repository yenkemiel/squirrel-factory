package com.uch.finalproject.model;

import lombok.Data;

@Data
public class LoginResponse extends BaseResponse {
    public LoginResponse(int code, String message) {
        super(code, message);
    }
}
