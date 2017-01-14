package com.imudges.controller;

import com.imudges.model.BaseEntity;
import com.imudges.model.StudentEntity;
import com.imudges.repository.StudentRepository;
import com.imudges.utils.MailSender;
import com.imudges.utils.SHA256Test;
import com.imudges.utils.VerifyCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;



/**
 * Created by cyy on 2016/12/9.
 */
@Controller
public class UserController {
     private StudentEntity studentEntity;

     @Autowired
     private StudentRepository studentRepository;
     @RequestMapping(value = "/login.html", method = RequestMethod.GET)
     public String ToLogin(){
          return "login";
     }

     @RequestMapping(value = "/register.html", method = RequestMethod.GET)
     public String ToRegistern(){
          return "register";
     }

    @ResponseBody
     @RequestMapping(value = "/user_register")
     public StudentEntity Register(String email,String password){
        studentEntity = studentRepository.findByEmail(email);
        if(studentEntity==null) {
            studentEntity = new StudentEntity();
            studentEntity.setEmail(email);
            studentEntity.setPassword(password);
            studentRepository.saveAndFlush(studentEntity);
            studentEntity.setStatus(0);
        }
        else {
            studentEntity = new StudentEntity();
            studentEntity.setStatus(102);
        }
        return studentEntity;
     }

    @ResponseBody
    @RequestMapping(value = "/userLogin")
    public StudentEntity userLogin(String email,String password){
        studentEntity = studentRepository.findByEmail(email);
        if(studentEntity==null){
            studentEntity = new StudentEntity();
            studentEntity.setStatus(100);
        }
        else if(!studentEntity.getPassword().equals(password)){
            studentEntity = new StudentEntity();
            studentEntity.setStatus(101);
        }
        else {
            String cookie = SHA256Test.SHA256Encrypt(email+new Date().toString());
            studentEntity.setCookie(cookie);

            studentRepository.saveAndFlush(studentEntity);
            studentEntity.setStatus(0);
        }
        return studentEntity;
    }

    @ResponseBody
    @RequestMapping(value = "/email_activate")
    public BaseEntity ActivateEmail(String cookie,HttpServletRequest request){
        studentEntity=studentRepository.findByCookie(cookie);
        String subject = "";
        StringBuffer url = new StringBuffer();
        StringBuilder builder = new StringBuilder();
        // 判断是否已激活
        if ("1".equals(String.valueOf(studentEntity.getNowstatus()))) {
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.setStatus(106);
            return baseEntity;
        }
        String contextPath = request.getContextPath();
        String rUrl = String.valueOf(request.getRequestURL());
        url.append(rUrl.substring(0, rUrl.indexOf("/email_activate")));
        //url.append(contextPath + "/account");

         url.append("/activateEmail?"+"cookie=" + studentEntity.getCookie() );
        //url.append("/activateEmail.jhtml?id=" + uid + "&mode=activate");
        // 正文
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" </head><body>");
        builder.append("请点击下方链接激活您的邮箱，完成激活邮箱的操作!");
        builder.append("<br/><br/>");
        builder.append("<a href=\"" + url + "\">");
        builder.append(url);
        builder.append("</a>");
        builder.append("</body></html>");
        subject = "邮箱地址激活 - xxxx";

        MailSender.mailSimple(studentEntity.getEmail(), subject, builder.toString());
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setStatus(0);
        return baseEntity;
    }


    @RequestMapping(value = "/activateEmail")
    @ResponseBody
    public BaseEntity activateEmail(String cookie) {
        // 激活之前查询
        studentEntity = studentRepository.findByCookie(cookie);

        if ("1".equals(String.valueOf(studentEntity.getNowstatus()))) {
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.setStatus(106);
            return baseEntity;

        } else {
            // 未激活
            studentEntity.setNowstatus("1");
            studentRepository.saveAndFlush(studentEntity);
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.setStatus(0);
            return baseEntity;
            // 激活之后查询
           /* user = userManager.find(params.getLong("id"));
            request.getSession().setAttribute("sessionUser", user);
            model.addAttribute("mode", params.getString("mode"));
            model.addAttribute("flag", true);*/
        }
        //return "site/modules/account/activateSuccess";
    }

    @RequestMapping(value = "/sendEmail")
    @ResponseBody
    public BaseEntity sendEmail(HttpServletRequest request,String cookie) {

        studentEntity=studentRepository.findByCookie(cookie);
        StringBuilder builder = new StringBuilder();
        StringBuffer url = new StringBuffer();
        String subject = "";
        // type = forget 密码重置
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        studentEntity.setSecurityCode(verifyCode);
        studentRepository.saveAndFlush(studentEntity);
        request.getSession().setAttribute("resetCertCode", verifyCode);
        url.append("<font color='red'>" + verifyCode + "</font>");
        // 正文
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head><body>");
        builder.append("要使用新的密码, 请将已下字符输入验证框中，完成重置密码的操作!");
        builder.append("<br/><br/>");
        builder.append("<div>" + url + "</div>");
        builder.append("</body></html>");
        subject = "密码重置 - xxxx";

        MailSender.mailSimple(studentEntity.getEmail(), subject, builder.toString());
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setStatus(0);
        return baseEntity;
    }

    @RequestMapping(value = "/ChangePasw")
    @ResponseBody
    public BaseEntity ChangePasw(String verifyCode,String cookie,String newpassword){
        studentEntity=studentRepository.findByCookie(cookie);
        BaseEntity baseEntity = new BaseEntity();
        if(studentEntity.getSecurityCode().equals(verifyCode)){
            baseEntity.setStatus(0);
            studentEntity.setPassword(newpassword);
            studentRepository.saveAndFlush(studentEntity);
        }else {
            baseEntity.setStatus(107);
        }
        return baseEntity;
    }

    @RequestMapping(value = "/ModfityStudent")
    @ResponseBody
    public StudentEntity ModfityStudent(String cookie,int age, String phone,String address,String information){
        studentEntity=studentRepository.findByCookie(cookie);
        studentEntity.setAddress(address);
        studentEntity.setAge(age);
        studentEntity.setInformation(information);
        studentEntity.setPhone(phone);
        studentRepository.saveAndFlush(studentEntity);
        return studentEntity;
    }
}
