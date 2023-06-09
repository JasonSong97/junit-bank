package shop.mtcoding.bank.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountRequestDto.AccountSaveReqeustDto;
import shop.mtcoding.bank.dto.account.AccountRequestDto.AccountTransferRequestDto;
import shop.mtcoding.bank.dto.account.AccountRequestDto.AccountWithdrawRequestDto;
import shop.mtcoding.bank.dto.user.UserRequestDto.AccountDepositRequestDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@Sql("classpath:db/teardown.sql")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class AccountControllerTest extends DummyObject {

     @Autowired
     private MockMvc mvc;
     @Autowired
     private ObjectMapper om;
     @Autowired
     private UserRepository userRepository;
     @Autowired
     private AccountRepository accountRepository;
     @Autowired
     private TransactionRepository transactionRepository;
     @Autowired
     private EntityManager em;

     @BeforeEach
     public void setUp() {
          dataSetting();
          em.clear();
     }

     // jwt token -> 인증필터 -> 시큐리티 세션생성
     // setupBefore=TEST_METHOD (setUp 메서드 실행전에 수행)
     // setupBefore=TEST_EXECUTION (saveAccount_test 메소드 실행전에 수행)
     @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION) // DB에서 username=ssar 조회를 해서 세션에
                                                                                       // 담아주는 어노테이션
     @Test
     public void saveAccount_test() throws Exception {
          // given
          AccountSaveReqeustDto accountSaveReqeustDto = new AccountSaveReqeustDto();
          accountSaveReqeustDto.setNumber(9999L);
          accountSaveReqeustDto.setPassword(1234L);
          String requestBody = om.writeValueAsString(accountSaveReqeustDto);
          System.out.println("테스트 : " + requestBody);

          // when
          ResultActions resultActions = mvc
                    .perform(post("/api/s/account").content(requestBody).contentType(MediaType.APPLICATION_JSON));
          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          resultActions.andExpect(status().isCreated());
     }

     /**
      * 테스트시에는 insert 한것들이 전부 PC에 올라감(영속화)
      * 영속화 된것들을 초기화 해주는 것이 개발 모드와 동일한 환경으로 테스트를 할 수 있께 해준다.
      * 최초에 select는 쿼리 발생하지만, PC에 있으면 캐싱해서 가져온다.
      * lazyloading은 쿼리도 발생안함 - PC에 있다면
      * lazyloading할 떄 PC에 없다면 쿼리가 발생
      */
     @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION) // ssar 로그인
     @Test
     public void deleteAccount_test() throws Exception {
          // given
          Long number = 1111L;

          // when
          ResultActions resultActions = mvc
                    .perform(delete("/api/s/account/" + number));
          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          // junit test에서 delete 쿼리는 DB관련(DML)으로 가장 마지막에 실행되면 발동안됨.
          assertThrows(CustomApiException.class, () -> accountRepository.findByNumber(number).orElseThrow(
                    () -> new CustomApiException("계좌를 찾을 수 없습니다.")));
     }

     @Test
     public void depositAccount_test() throws Exception {
          // given
          AccountDepositRequestDto accountDepositRequestDto = new AccountDepositRequestDto();
          accountDepositRequestDto.setNumber(1111L);
          accountDepositRequestDto.setAmount(100L);
          accountDepositRequestDto.setGubun("DEPOSIT");
          accountDepositRequestDto.setTel("01088887777");

          String requestBody = om.writeValueAsString(accountDepositRequestDto);
          System.out.println("테스트 : " + requestBody);

          // when
          ResultActions resultActions = mvc
                    .perform(post("/api/account/deposit").content(requestBody).contentType(MediaType.APPLICATION_JSON));
          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          resultActions.andExpect(status().isCreated());
     }

     @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
     @Test
     public void withdrawAccount_test() throws Exception {
          // given
          AccountWithdrawRequestDto accountWithdrawRequestDto = new AccountWithdrawRequestDto();
          accountWithdrawRequestDto.setNumber(1111L);
          accountWithdrawRequestDto.setPassword(1234L);
          accountWithdrawRequestDto.setAmount(100L);
          accountWithdrawRequestDto.setGubun("WITHDRAW");

          String requestBody = om.writeValueAsString(accountWithdrawRequestDto);
          System.out.println("테스트 : " + requestBody);

          // when
          ResultActions resultActions = mvc
                    .perform(post("/api/s/account/withdraw").content(requestBody)
                              .contentType(MediaType.APPLICATION_JSON));
          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          resultActions.andExpect(status().isCreated());
     }

     @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
     @Test
     public void transferAccount_test() throws Exception {
          // given
          AccountTransferRequestDto accountTransferRequestDto = new AccountTransferRequestDto();
          accountTransferRequestDto.setWithdrawNumber(1111L);
          accountTransferRequestDto.setDepositNumber(2222L);
          accountTransferRequestDto.setWithdrawPassword(1234L);
          accountTransferRequestDto.setAmount(100L);
          accountTransferRequestDto.setGubun("TRANSFER");

          String requestBody = om.writeValueAsString(accountTransferRequestDto);
          System.out.println("테스트 : " + requestBody);

          // when
          ResultActions resultActions = mvc
                    .perform(post("/api/s/account/transfer").content(requestBody)
                              .contentType(MediaType.APPLICATION_JSON));
          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          resultActions.andExpect(status().isCreated());
     }

     @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION) // 인증이 필요
     @Test
     public void findDetailAccount_test() throws Exception {
          // given
          Long number = 1111L;
          String page = "0";

          // when
          ResultActions resultActions = mvc
                    .perform(get("/api/s/account/" + number).param("page",
                              page));

          String responseBody = resultActions.andReturn().getResponse().getContentAsString();
          System.out.println("테스트 : " + responseBody);

          // then
          resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(900L));
          resultActions.andExpect(jsonPath("$.data.transactions[1].balance").value(800L));
          resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(700L));
          resultActions.andExpect(jsonPath("$.data.transactions[3].balance").value(800L));

     }

     private void dataSetting() {
          User ssar = userRepository.save(newUser("ssar", "쌀"));
          User cos = userRepository.save(newUser("cos", "코스,"));
          User love = userRepository.save(newUser("love", "러브"));
          User admin = userRepository.save(newUser("admin", "관리자"));

          Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
          Account cosAccount = accountRepository.save(newAccount(2222L, cos));
          Account loveAccount = accountRepository.save(newAccount(3333L, love));
          Account ssarAccount2 = accountRepository.save(newAccount(4444L, ssar));

          Transaction withdrawTransaction1 = transactionRepository
                    .save(newWithdrawTransaction(ssarAccount1, accountRepository));

          Transaction depositTransaction1 = transactionRepository
                    .save(newDepositTransaction(cosAccount, accountRepository));

          Transaction transferTransaction1 = transactionRepository
                    .save(newTransferTransaction(ssarAccount1, cosAccount, accountRepository));
          Transaction transferTransaction2 = transactionRepository
                    .save(newTransferTransaction(ssarAccount1, loveAccount, accountRepository));
          Transaction transferTransaction3 = transactionRepository
                    .save(newTransferTransaction(cosAccount, ssarAccount1, accountRepository));
     }
}