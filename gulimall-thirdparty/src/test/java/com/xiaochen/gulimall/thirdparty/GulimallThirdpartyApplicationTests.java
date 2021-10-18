package com.xiaochen.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

@SpringBootTest
class GulimallThirdpartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Test
    void contextLoads() {
    }

    @Autowired
    private SmsComponent smsComponent;

    @Test
    void upLoad() throws FileNotFoundException {
        InputStream inputStream=new FileInputStream("D:\\IntellijIdeaWorkSpace\\gulimall\\gulimall-product\\src\\main\\resources\\static\\Apple.jpg");
        ossClient.putObject("gulimall-chenxiao","Apple2.jpg",inputStream);
    }

    @Test
    public void test(){
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 6)+"_"+System.currentTimeMillis();
        smsComponent.sendSmsCode("18728669164",code);
    }

}
