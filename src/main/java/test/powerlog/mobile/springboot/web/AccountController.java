package test.powerlog.mobile.springboot.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.engine.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import test.powerlog.mobile.springboot.domain.products.*;
import test.powerlog.mobile.springboot.web.dto.EmailDto;
import test.powerlog.mobile.springboot.service.*;
import test.powerlog.mobile.springboot.web.dto.LogLateMsrDto;
import test.powerlog.mobile.springboot.web.dto.SignUpDto;
import test.powerlog.mobile.springboot.web.dto.UserAccountDto;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
public class AccountController {

    @Autowired
    private UserAccountVwRepository userAccountVWRepository;

    @Autowired
    private LogLateMsrVwRepository logLateMsrVwRepository;

    @Autowired
    LoginService loginService;

    @Autowired
    EmailPhoneCheckService emailPhoneCheckService;

    @Autowired
    EmailQuestionCheckService emailQuestionCheckService;

    @Autowired
    SignUpService signUpService;

    @Autowired
    ResetPasswordService resetPasswordService;

    @PostMapping(value = "/login")
    public HashMap<String, Object> Login(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {
        HashMap<String, Object> resultMap = new HashMap();
        String email = userAccountDto.getEmail();
        String password =userAccountDto.getPassword();
        LogLateMsrDto logLateMsrDto = new LogLateMsrDto();
        try{
            // match 경우 있거나, 아이디는 존재하는데 비밀번호 틀린 경우

//            userAccountVWRepository.findById(email).get().getLoginVwEmail();
//            logLateMsrVwRepository.findAllByLgLateMsrVwEmail(email);
            //아이디(이메일)이 존재하지 않는 경우 여기서 catch로 넘어가게 될 것임
            Boolean result = loginService.Login(email, password);
//            resultMap.put("received_password", userAccountDto.getPassword());
//            resultMap.put("received_email", userAccountDto.getEmail());
            resultMap.put("name", userAccountVWRepository.findById(email).get().getLoginVwName());
            resultMap.put("result", logLateMsrVwRepository.findAllByLgLateMsrVwEmail(userAccountDto.getEmail()));
            resultMap.put("match", result);
            resultMap.put("error", null);
        }
        // 아이디가 아예 존재하지 않는 경우
        catch(Exception ex){
//            resultMap.put("received_password", userAccountDto.getPassword());
//            resultMap.put("received_email", userAccountDto.getEmail());
            resultMap.put("name", null);
            resultMap.put("result", null);
            resultMap.put("match", false);
            resultMap.put("error", ex.toString());
        }
        return resultMap;
    }

    @PostMapping(value = "/signUpCheckEmail")
    public HashMap<String, Object> SignUpCheckEmail(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {
        HashMap<String, Object> resultMap = new HashMap();
        String id = userAccountDto.getEmail();

        //중복 아이디가 있는 경우
        try{
            System.out.println(userAccountVWRepository.findById(id).get().getLoginVwEmail());
            //같은 아이디를 찾을 수 없다면 여기서 catch로 넘어가게 될 것임
//            resultMap.put("receivedEmail", userAccountDto.getEmail());
            resultMap.put("emailPresent", true);
            resultMap.put("error", null);
        }
        // 중복 아이디가 없는 경우
        catch(Exception ex){
//            resultMap.put("receivedEmail", userAccountDto.getEmail());
            resultMap.put("emailPresent", false);
            resultMap.put("error", ex.toString());
        }
        return resultMap;
    }

    @PostMapping(value = "/signUpSendMsg")
    public HashMap<String, Object> SignUpSendMsg(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {
        HashMap<String, Object> tmpMap = new HashMap();
        HashMap<String, Object> resultMap = new HashMap();
        SendMsgService_New sendMsgService_new = new SendMsgService_New();
        String phone = userAccountDto.getPhone();
        ObjectMapper mapper = new ObjectMapper();
        NumberGen numberGen = new NumberGen();
        String randNum = numberGen.four_digits(4, 1);
        String[] numbers = {"99999999999"};
        try{
            userAccountVWRepository.findByLoginVwPhone(phone).getLoginVwPhone();
            resultMap.put("phonePresent", true);
            resultMap.put("verificationNum", randNum);
            resultMap.put("error", "isPresentError");
        }
        catch(Exception ex){
            System.out.println(ex);
//             전화번호가 존재하지 않은 경우에만 메시지를 보낸다

            numbers[0] = phone;
            tmpMap.put("type", "SMS");
            tmpMap.put("from", "01050055438");
            tmpMap.put("to", numbers);
            tmpMap.put("content", "인증번호 [" + randNum + "] 숫자 4자리를 입력해주세요 - 파워로그 모바일");
            String json = mapper.writeValueAsString(tmpMap);

            sendMsgService_new.NewSend("https://api-sens.ncloud.com/v1/sms/services/ncp:sms:kr:258080742855:testpowerlog/messages", json);

            resultMap.put("phonePresent", false);
            resultMap.put("verificationNum", randNum);
            resultMap.put("error", null);
        }
        return resultMap;
    }

    @PostMapping(value = "/signUp")
    public HashMap<String, Object> SignUp(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {

        HashMap<String, Object> resultMap = new HashMap();
        NumberGen numberGen = new NumberGen();

        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        String tmpUid = numberGen.four_digits(8, 1);
        int careerY = userAccountDto.getCareer_year();
        int careerM = userAccountDto.getCareer_month();

        System.out.println(userAccountDto.getQuestionCode());
        System.out.println(userAccountDto.getQuestionAnswer());



        SignUpDto signUpDto  = SignUpDto.builder().email(userAccountDto.getEmail()).password(userAccountDto.getPassword()).uid(tmpUid).name(userAccountDto.getName())
                .gender(userAccountDto.getGender()).birth(userAccountDto.getBirth()).height(userAccountDto.getHeight()).weight(userAccountDto.getWeight())
                .agreeFlag(userAccountDto.getAgreeFlag()).personalFlag(userAccountDto.getAgreeFlag()).shapeCode(userAccountDto.getShapeCode()).qAnswer(userAccountDto.getQuestionAnswer()).qCode(userAccountDto.getQuestionCode())
                .verification(userAccountDto.getVerification()).phone(userAccountDto.getPhone()).createdTime(sqlDate).updatedTime(sqlDate).career(careerM + careerY * 12).build();
        signUpService.Signup(signUpDto); // save 실행

        try{
            String findById = userAccountVWRepository.findById(userAccountDto.getEmail()).get().getLoginVwEmail();
            resultMap.put("findById", findById);
            resultMap.put("result", true);
            resultMap.put("error", null);
        }
        catch(Exception ex){
            resultMap.put("findById", ex);
            resultMap.put("result", false);
            resultMap.put("error", ex.toString());
        }

        return resultMap;
    }

    @PostMapping(value = "/resetCheckEmailPhone")
    public HashMap<String, Object> ResetCheckEmailPhone(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {

        HashMap<String, Object> resultMap = new HashMap();
        HashMap<String, Object> tmpMap = new HashMap();
        NumberGen numberGen = new NumberGen();
        ObjectMapper mapper = new ObjectMapper();
        SendMsgService_New sendMsgService_new = new SendMsgService_New();

        String phone = userAccountDto.getPhone();
        String email = userAccountDto.getEmail();
        String[] numbers = {"99999999999"};
        String randNum =  numberGen.four_digits(4, 1);

        try{
            Boolean result = emailPhoneCheckService.emailPhoneCheck(email, phone);
            if(result){
                numbers[0] = phone;
                tmpMap.put("type", "SMS");
                tmpMap.put("from", "01050055438");
                tmpMap.put("to", numbers);
                tmpMap.put("content", "인증번호 [" + randNum + "] 숫자 4자리를 입력해주세요 - 파워로그 모바일");
                String json = mapper.writeValueAsString(tmpMap);

                sendMsgService_new.NewSend("https://api-sens.ncloud.com/v1/sms/services/ncp:sms:kr:258080742855:testpowerlog/messages", json);
            }
            resultMap.put("verificationNum", randNum);
            resultMap.put("match", result);
            resultMap.put("error", null);
        }
        catch(Exception ex){
            resultMap.put("verificationNum", randNum);
            resultMap.put("match", false);
            resultMap.put("error", ex.toString());
            System.out.println(ex);
        }
        return resultMap;
    }

    @PostMapping(value = "/resetCheckEmailQuestion")
    public HashMap<String, Object> ResetCheckEmailQuestion(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {

        HashMap<String, Object> resultMap = new HashMap();
        NumberGen numberGen = new NumberGen();

        String email = userAccountDto.getEmail();
        String questionCode = userAccountDto.getQuestionCode();
        String questionAnswer = userAccountDto.getQuestionAnswer();
        String randNum =  numberGen.four_digits(12, 1);

        try{
            Boolean result = emailQuestionCheckService.emailQuestionCheck(email,questionCode, questionAnswer, randNum);
            resultMap.put("emailPresent", true);
            resultMap.put("match", result);
            resultMap.put("error", null);
        }
        catch(Exception ex){
            resultMap.put("emailPresent", null);
            resultMap.put("match", false);
            resultMap.put("error", ex.toString());
            System.out.println(ex);
        }
        return resultMap;
    }

    @PostMapping(value = "/resetPassword")
    public HashMap<String, Object> ResetPassword(@RequestBody UserAccountDto userAccountDto) throws JsonProcessingException {

        HashMap<String, Object> resultMap = new HashMap();

        String email = userAccountDto.getEmail();
        String password = userAccountDto.getPassword();

        try{
            Boolean result = resetPasswordService.ResetPassword(email, password);
            resultMap.put("completed", result);
            resultMap.put("error", null);
        }
        catch(Exception ex){
//            resultMap.put("verificationNum", randNum);
            resultMap.put("completed", false);
            resultMap.put("error", ex.toString());
            System.out.println(ex);
        }
        return resultMap;
    }

    @GetMapping("/create")
    public void create(UserAccountVw product) {
        userAccountVWRepository.save(product);
    }

    @GetMapping("/delete")
    public void delete(String id) {
        userAccountVWRepository.deleteById(id);

        //    @PostMapping(value = "/resetSendMail") // 얘는 서비스로 옮겨야 한다.
//    public SignUpDto ResetSendMail(@RequestBody EmailDto emailDto) throws JsonProcessingException {
//        SignUpDto signupDto = new SignUpDto();
//        HashMap<String, Object> resultMap = new HashMap();
//
//        try{
//            Optional<UserTb> signUpDto = userTbRepository.findById(emailDto.getRecipient());
//        }
//        catch (Exception ex){
//            System.out.println(ex);
//        }
//        try{
//            emailService.sendMail(emailDto);
//        }
//        catch(Exception ex){
//            resultMap.put("findById", ex);
//            resultMap.put("result", "false");
//        }
//
//        return signupDto;
//    }
    }
}
