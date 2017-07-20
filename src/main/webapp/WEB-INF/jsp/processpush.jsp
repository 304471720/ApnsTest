<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

%>
<!DOCTYPE html>

<html>
  <head>

    <title>login</title>
	 <script type="text/javascript" src="js/jquery-1.7.2.js" ></script>
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="this is my page">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    
    <!--<link rel="stylesheet" type="text/css" href="./styles.css">-->

  </head>
  
  
  	<script type="text/javascript">
  	
  	
   $(document).ready(function(){	 
  
  	function getback(result){
  	
  		var url = "";
	  			$.each(result[0],function(key,value){ url = value;  });
	  			if(url.search("jsp|html")!=-1){
	  				window.location   =   url;
	  			}else{
	  				var html = "<table  border='1'  style='table-layout:fixed;' colore = 'gree'> ";
  		$.each(result,function(index){ 
  				
  			if(index==0)
	  			 {
				  	html = html+"<tr>";
				  	$.each(result[0],function(key,value)
					  	{ 
					  		html = html+"<th>"+key+"</th>";  
					  	});
				  	html = html+"</tr>";
				  }
              var ht = "<tr>";
                           	 
              $.each(result[index],function(key,value)
	              { 
	              	ht = ht+"<td>"+value+"</td>";  
	              });
                           	 
			   html = html+ht+"</tr>";
  				
  				});
  		html= html + "</table>";
        $("#xianshi").html(html);
	  			}
  }
  	
	  $("#login").click(function(){
  	
  		var messagename = "login";
  		var email = $("#email").val();
  		var captcha = $("#captcha").val();
		
	  	$.post("servlet/AjaxServlet",{messagename:messagename,email:email,captcha:captcha},function(result){
	  			var url = "";
	  			$.each(result[0],function(key,value){ url = value;  });
	  			if(url.length==16){
	  				window.location   =   url;
	  			}else{
	  				getback(result);
	  			}
	  			
	  	
	  	},"json");
	  	
	  });
	  
	  
	  $("#query").click(function(){
  		var messagename = "query";
  		var datebase = $("input[name='database']:checked").val();
  		 
  		var operation = $("input[name='operation']:checked").val();
  		var sql = $("#sql").val();
  		
	  	 $.post("servlet/AjaxServlet",{messagename:messagename,datebase:datebase,sql:sql,operation:operation},function(result){
	  			getback(result);
	  	},"json");
	  	 
	  });
	  
	  
	   $("#out").click(function(){
  		var messagename = "out";
  		
	  	 $.post("servlet/AjaxServlet",{messagename:messagename},function(result){
	  			getback(result);
	  	},"json");
	  	 
	  });

	    $("#push").click(function(){
	    $("#push").attr('disabled',true);
  		var messagename = "";
  		var product = $("input[name='product']:checked").val();
  		var isTest = $("input[name='env']:checked").val();
  		var os = $("input[name='os']:checked").val();
  		var type = $("input[name='type']:checked").val();
  		var token = $("#token").val();
  		var message = $("#message").val();
  		//alert("product:"+product+",env:"+env+",os:"+os+",type:"+type+",token:"+token+",message:"+message);
  		
	  	 $.post("servlet/newapns",{product:product,isTest:isTest,os:os,token:token,message:message,type:type},function(result){
	  			// $("#xianshi").html(result);
	  			 getback(result);
	  	},"json");
	  	 refreshCount(30);
	  });
	   
	 });   
  
  
  function refreshCount(timecount){
		$("#push").attr('disabled',true).val('再次提交('+timecount+')');
		timecount--;
		if(timecount>-1){
			setTimeout('refreshCount('+timecount+')',1000)
		}else{
			$("#push").attr('disabled',false).val('提交');
		}
	}
	</script>
  
  <body>
  <!-- <form action="./servlet/AjaxServlet?messagename=login" method="post"> -->
<div id="table" align="center">
         产品:
         产品A:<input type="radio" name="product"   value="soufun" >
         产品B:<input type="radio" name="product"  value="agent">
   房天下装修帮:<input type="radio" name="product"  value="sfzxapp">
        <br>
        <br>
          环境:
          正式:<input type="radio" name="env"  id="env" value="1">
          测试:<input type="radio" name="env" id="env" value="0">
       
     	 <br>
         <br>
         
         封装message:
          是:<input type="radio" name="type"  id="type" value="1">
          否:<input type="radio" name="type" id="type" value="0">
       
     	   <br>
         <br>
         
            操作系统:
          iPhone:<input type="radio" name="os"  id="os" value="iphone">
          ipad:<input type="radio" name="os" id="os" value="ipad">
       
     	   <br>
         <br>
                <br>
          token:
          <textarea  name="token" id="token" cols="100"></textarea> 
          <br>
           <br>
          message:
          <textarea  name="message" id="message" cols="100" rows="3"></textarea> 
          <br>
           <br>
        <br>
          <input type="submit" id="push">
           <br>
           
              <br>
      <div id="xianshi" align="center"></div>
    <!-- </form> -->
    </div>
  </body>
</html>
