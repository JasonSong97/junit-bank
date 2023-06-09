package shop.mtcoding.bank.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

     // select * from user where username = ?
     Optional<User> findByUsername(String username); // JPA 네임쿼리 작동

}
