package shop.mtcoding.bank.dto.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class UserRequestDto {

     @ToString
     @Getter
     @Setter
     public static class JoinRequestDto { // validation check
          private String username;
          private String password;
          private String email;
          private String fullname;

          public User toEntity(BCryptPasswordEncoder passwordEncoder) {
               return User.builder()
                         .username(username)
                         .password(passwordEncoder.encode(password))
                         .email(email)
                         .fullname(fullname)
                         .role(UserEnum.CUSTOMER)
                         .build();
          }

     }
}