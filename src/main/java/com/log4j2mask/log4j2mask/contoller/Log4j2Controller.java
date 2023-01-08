package com.log4j2mask.log4j2mask.contoller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class Log4j2Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Log4j2Controller.class);

    @GetMapping("/test")
    public String testGet() {
        String password = "abc";
        long creditCard = 1234567890l;
        //LOGGER.info("this is user 1 password=" + password + " with =");
        /*LOGGER.info("this is user 2 password:" + password + " with :");
        LOGGER.info("this is user 3 password" + password + " with empty");
        LOGGER.info("this is user 4 password " + password + " with space");*/
        LOGGER.info("<DOB>24.05.1988</DOB>");
        System.out.println();
        LOGGER.info("<emp><PLACE>BANGALORE</PLACE><DOB>24.05.1988</DOB></emp>");
        System.out.println();
        LOGGER.info("{\n" +
                "  \"emp\": {\n" +
                "    \"sal\": \"10000$\",\n" +
                "    \"account\": \"1234567890\"\n" +
                "  }\n" +
                "}");

        //LOGGER.info("this is user 1 creditCard=" + creditCard + " with =" );
        /*LOGGER.info("this is user 2 creditCard:" + creditCard + " with :");
        LOGGER.info("this is user 3 creditCard" + creditCard + " with empty");
        LOGGER.info("this is user 4 creditCard " + creditCard + " with space");*/
        return "OK";
    }

}