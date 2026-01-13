package com.jaehyun.demo.dto.request.auth;

import com.jaehyun.demo.core.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    private String email;
    private String password;
    private String name;
    private Role type;
}
