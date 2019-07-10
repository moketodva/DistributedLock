package com.test.redis;

import com.test.redis.common.RedisLock;
import com.test.redis.common.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisApplicationTests {

    @Resource
    private RedisLock redisLock;
    private CountDownLatch countDownLatch = new CountDownLatch(100);
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(101);


    @Test
    public void test() throws InterruptedException, BrokenBarrierException {
        Task task = new Task(redisLock, countDownLatch, cyclicBarrier);
        for (int i = 0; i < 100; i++){
            new Thread(task).start();
            countDownLatch.countDown();
        }
        cyclicBarrier.await();
        System.out.println("结束...");
    }
}
