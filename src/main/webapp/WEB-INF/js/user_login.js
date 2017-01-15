/**
 * Created by 隐 on 2016/12/18.
 */

$(document).ready(function (){
   $("#submit").click(function(){
       alert("user_ajax");
       $.ajax({
           // url:'http://localhost:8080/userLogin',
           url:"/userLogin",
           type:'post',
           dataType:'json',
           async: false,
           data:$("#user_login_ajax").serialize(),
           error: function(request) {
               console.log(request);
               alert("Connection error");
           },
           success:function(json){

               alert(JSON.stringify(json));

               var status=json.status;
               if(status=='0'){
                   // window.location.href = "admin/index.action";    //跳转到后台主页
                    alert("success");
                   window.opener=null;
                   window.open('index.html','_self');

               }else{
                   var error=document.getElementsByClassName("error");
                   if(status=="100"){
                       window.opener=null;
                       window.open('login.html','_self');

                       error[0].style.display="block";

                   }else{
                       if(status=="101"){

                           window.opener=null;
                           window.open('login.html','_self');

                           error[1].style.display="block";

                       }
                   }


               }

           }

       });
   });
});

