package com.test.redis.common;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author moke
 */
public class Task implements Runnable {
    private Lock redisLock;
    private CountDownLatch countDownLatch;
    private CyclicBarrier cyclicBarrier;

    public Task(RedisLock redisLock, CountDownLatch countDownLatch, CyclicBarrier cyclicBarrier){
        this.redisLock = redisLock;
        this.countDownLatch = countDownLatch;
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try{
            countDownLatch.await();
            System.out.println("线程" + Thread.currentThread().getName() + "抢锁开始...");
            redisLock.lock();
            System.out.println("线程" + Thread.currentThread().getName() + "加锁成功");
            Thread.sleep(300);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisLock.unLock();
            System.out.println("线程" + Thread.currentThread().getName() + "解锁成功");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
